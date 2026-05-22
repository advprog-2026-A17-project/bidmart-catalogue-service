package id.ac.ui.cs.advprog.bidmartcatalogueservice.controller;

import id.ac.ui.cs.advprog.bidmartcatalogueservice.dto.AdminListingActionRequest;
import id.ac.ui.cs.advprog.bidmartcatalogueservice.dto.BidPlacedEvent;
import id.ac.ui.cs.advprog.bidmartcatalogueservice.dto.ListingClosedByAdminEvent;
import id.ac.ui.cs.advprog.bidmartcatalogueservice.dto.ListingCreatedEvent;
import id.ac.ui.cs.advprog.bidmartcatalogueservice.service.CatalogAccessPolicy;
import id.ac.ui.cs.advprog.bidmartcatalogueservice.dto.ListingSummaryResponse;
import id.ac.ui.cs.advprog.bidmartcatalogueservice.event.ListingEventPublisher;
import id.ac.ui.cs.advprog.bidmartcatalogueservice.metrics.BidmartCatalogueMetrics;
import id.ac.ui.cs.advprog.bidmartcatalogueservice.model.Listing;
import io.micrometer.core.annotation.Timed;
import id.ac.ui.cs.advprog.bidmartcatalogueservice.model.ListingStatus;
import id.ac.ui.cs.advprog.bidmartcatalogueservice.service.ListingService;
import id.ac.ui.cs.advprog.bidmartcatalogueservice.util.ListingPresentation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/catalogue/listings")
public class ListingController {

    private final ListingService listingService;
    private final ListingEventPublisher listingEventPublisher;
    private final BidmartCatalogueMetrics catalogueMetrics;
    private final CatalogAccessPolicy catalogAccessPolicy;

    public ListingController(
            ListingService listingService,
            ListingEventPublisher listingEventPublisher,
            BidmartCatalogueMetrics catalogueMetrics,
            CatalogAccessPolicy catalogAccessPolicy
    ) {
        this.listingService = listingService;
        this.listingEventPublisher = listingEventPublisher;
        this.catalogueMetrics = catalogueMetrics;
        this.catalogAccessPolicy = catalogAccessPolicy;
    }

    @Timed(value = "bidmart.catalogue.create_listing", description = "Create catalogue listing")
    @PostMapping
    public ResponseEntity<?> create(@RequestHeader("X-User-Id") String sellerId, @RequestBody Listing listing) {
        try {
            listing.setSellerId(sellerId);
            Listing created = listingService.createListing(listing);

            listingEventPublisher.publishListingCreated(new ListingCreatedEvent(
                    created.getId(),
                    created.getSellerId(),
                    created.getStartingPrice(),
                    created.getStatus()
            ));

            catalogueMetrics.recordListingCreated();
            return ResponseEntity.ok(ListingPresentation.forListResponse(created));
        } catch (IllegalArgumentException exception) {
            return ResponseEntity.badRequest().body(Map.of("message", exception.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<List<Listing>> getAll() {
        return ResponseEntity.ok(ListingPresentation.forListResponse(listingService.getAllListings()));
    }

    @GetMapping("/seller")
    public ResponseEntity<List<Listing>> getBySeller(@RequestHeader("X-User-Id") String sellerId) {
        return ResponseEntity.ok(listingService.getListingsBySeller(sellerId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Listing> getById(@PathVariable String id) {
        return ResponseEntity.ok(listingService.getListingById(id));
    }

    @GetMapping("/{id}/summary")
    public ResponseEntity<ListingSummaryResponse> getSummaryById(@PathVariable String id) {
        Listing listing = listingService.getListingById(id);
        if (listing == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(ListingSummaryResponse.builder()
                .id(listing.getId())
                .sellerId(listing.getSellerId())
                .status(listing.getStatus())
                .build());
    }

    @GetMapping("/search")
    public ResponseEntity<?> search(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String minPrice,
            @RequestParam(required = false) String maxPrice,
            @RequestParam(required = false) ListingStatus status,
            @RequestParam(required = false) String endBefore,
            @RequestParam(required = false) String endAfter,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "title") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {

        BigDecimal parsedMinPrice;
        BigDecimal parsedMaxPrice;
        LocalDateTime parsedEndBefore;
        LocalDateTime parsedEndAfter;
        try {
            parsedMinPrice = parseMoneyParam(minPrice, "minPrice");
            parsedMaxPrice = parseMoneyParam(maxPrice, "maxPrice");
            parsedEndBefore = parseDateTimeParam(endBefore, "endBefore");
            parsedEndAfter = parseDateTimeParam(endAfter, "endAfter");
        } catch (IllegalArgumentException exception) {
            return ResponseEntity.badRequest().body(Map.of("message", exception.getMessage()));
        }

        if (parsedMinPrice != null && parsedMaxPrice != null && parsedMinPrice.compareTo(parsedMaxPrice) > 0) {
            return ResponseEntity.badRequest().body(Map.of("message", "minPrice must be less than or equal to maxPrice"));
        }

        Pageable pageable = PageRequest.of(
                Math.max(0, page),
                Math.max(1, Math.min(size, 100)),
                sortFromRequest(sortBy, sortDir)
        );

        return ResponseEntity.ok(ListingPresentation.forListResponse(listingService.searchListings(
                category,
                categoryId,
                keyword,
                parsedMinPrice,
                parsedMaxPrice,
                status,
                parsedEndBefore,
                parsedEndAfter,
                pageable
        )));
    }

    private BigDecimal parseMoneyParam(String rawValue, String fieldName) {
        if (rawValue == null || rawValue.isBlank()) {
            return null;
        }
        try {
            BigDecimal value = new BigDecimal(rawValue.trim());
            if (value.compareTo(BigDecimal.ZERO) < 0) {
                throw new IllegalArgumentException(fieldName + " must be greater than or equal to 0");
            }
            return value;
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException(fieldName + " must be a valid number");
        }
    }

    private LocalDateTime parseDateTimeParam(String rawValue, String fieldName) {
        if (rawValue == null || rawValue.isBlank()) {
            return null;
        }
        String value = rawValue.trim();
        try {
            return OffsetDateTime.parse(value).withOffsetSameInstant(ZoneOffset.UTC).toLocalDateTime();
        } catch (DateTimeParseException ignored) {
            try {
                return LocalDateTime.parse(value);
            } catch (DateTimeParseException exception) {
                throw new IllegalArgumentException(fieldName + " must be an ISO date-time");
            }
        }
    }

    private Sort sortFromRequest(String rawSortBy, String rawSortDir) {
        String sortBy = rawSortBy == null ? "" : rawSortBy.trim();
        String sortDir = rawSortDir == null ? "" : rawSortDir.trim();
        if (sortBy.endsWith("-desc")) {
            sortDir = "desc";
            sortBy = sortBy.substring(0, sortBy.length() - "-desc".length());
        } else if (sortBy.endsWith("-asc")) {
            sortDir = "asc";
            sortBy = sortBy.substring(0, sortBy.length() - "-asc".length());
        }

        String property = switch (sortBy.replace("_", "").replace("-", "").toLowerCase()) {
            case "price", "currentprice", "topbid" -> "currentPrice";
            case "endingsoon", "endtime", "recent" -> "endTime";
            case "category" -> "category";
            case "title", "name" -> "title";
            default -> "title";
        };

        return "desc".equalsIgnoreCase(sortDir)
                ? Sort.by(property).descending()
                : Sort.by(property).ascending();
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable String id, @RequestHeader("X-User-Id") String sellerId, @RequestBody Listing listing) {
        Listing existingListing = listingService.getListingById(id);
        if (existingListing == null) {
            return ResponseEntity.notFound().build();
        }
        if (!existingListing.getSellerId().equals(sellerId)) {
            return ResponseEntity.status(org.springframework.http.HttpStatus.FORBIDDEN).build();
        }

        try {
            listing.setSellerId(sellerId);
            Listing updatedListing = listingService.updateListing(id, listing);
            return ResponseEntity.ok(ListingPresentation.forListResponse(updatedListing));
        } catch (IllegalStateException exception) {
            return ResponseEntity.status(409).body(Map.of("message", exception.getMessage()));
        } catch (IllegalArgumentException exception) {
            return ResponseEntity.badRequest().body(Map.of("message", exception.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id, @RequestHeader("X-User-Id") String sellerId) {
        Listing existingListing = listingService.getListingById(id);
        if (existingListing != null) {
            if (!existingListing.getSellerId().equals(sellerId)) {
                return ResponseEntity.status(org.springframework.http.HttpStatus.FORBIDDEN).build();
            }
            listingService.deleteListing(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping("/{id}/bid")
    public ResponseEntity<?> bidPlaced(@PathVariable String id, @RequestBody BidPlacedEvent event) {
        try {
            Listing updated = listingService.handleBidPlaced(id, event.amount());
            if (updated == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(ListingPresentation.forListResponse(updated));
        } catch (IllegalStateException exception) {
            return ResponseEntity.status(409).body(Map.of("message", exception.getMessage()));
        }
    }

    @Timed(value = "bidmart.catalogue.publish_listing", description = "Publish catalogue listing")
    @PostMapping("/{id}/publish")
    public ResponseEntity<?> publish(@PathVariable String id, @RequestHeader("X-User-Id") String sellerId) {
        Listing existingListing = listingService.getListingById(id);
        if (existingListing == null) {
            return ResponseEntity.notFound().build();
        }
        if (!existingListing.getSellerId().equals(sellerId)) {
            return ResponseEntity.status(org.springframework.http.HttpStatus.FORBIDDEN).build();
        }

        try {
            Listing published = listingService.publishListing(id);
            catalogueMetrics.recordListingPublished();
            return ResponseEntity.ok(ListingPresentation.forListResponse(published));
        } catch (IllegalStateException exception) {
            return ResponseEntity.status(409).body(Map.of("message", exception.getMessage()));
        } catch (IllegalArgumentException exception) {
            return ResponseEntity.badRequest().body(Map.of("message", exception.getMessage()));
        }
    }

    @PostMapping("/{id}/deactivate")
    public ResponseEntity<?> deactivate(@PathVariable String id, @RequestHeader("X-User-Id") String sellerId) {
        Listing existingListing = listingService.getListingById(id);
        if (existingListing == null) {
            return ResponseEntity.notFound().build();
        }
        if (!existingListing.getSellerId().equals(sellerId)) {
            return ResponseEntity.status(org.springframework.http.HttpStatus.FORBIDDEN).build();
        }

        try {
            Listing deactivated = listingService.deactivateListing(id);
            return ResponseEntity.ok(ListingPresentation.forListResponse(deactivated));
        } catch (IllegalStateException exception) {
            return ResponseEntity.status(409).body(Map.of("message", exception.getMessage()));
        }
    }

    @PostMapping("/{id}/extend")
    public ResponseEntity<?> markExtended(@PathVariable String id, @RequestHeader("X-User-Id") String sellerId) {
        Listing existingListing = listingService.getListingById(id);
        if (existingListing == null) {
            return ResponseEntity.notFound().build();
        }
        if (!existingListing.getSellerId().equals(sellerId)) {
            return ResponseEntity.status(org.springframework.http.HttpStatus.FORBIDDEN).build();
        }

        try {
            Listing result = listingService.markExtended(id);
            return ResponseEntity.ok(ListingPresentation.forListResponse(result));
        } catch (IllegalStateException exception) {
            return ResponseEntity.status(409).body(Map.of("message", exception.getMessage()));
        }
    }

    @PostMapping("/{id}/close")
    public ResponseEntity<?> markClosed(@PathVariable String id, @RequestHeader("X-User-Id") String sellerId) {
        Listing existingListing = listingService.getListingById(id);
        if (existingListing == null) {
            return ResponseEntity.notFound().build();
        }
        if (!existingListing.getSellerId().equals(sellerId)) {
            return ResponseEntity.status(org.springframework.http.HttpStatus.FORBIDDEN).build();
        }

        try {
            Listing result = listingService.markClosed(id);
            return ResponseEntity.ok(ListingPresentation.forListResponse(result));
        } catch (IllegalStateException exception) {
            return ResponseEntity.status(409).body(Map.of("message", exception.getMessage()));
        }
    }

    @PostMapping("/{id}/won")
    public ResponseEntity<?> markWon(@PathVariable String id, @RequestHeader("X-User-Id") String sellerId, @RequestBody Map<String, BigDecimal> body) {
        Listing existingListing = listingService.getListingById(id);
        if (existingListing == null) {
            return ResponseEntity.notFound().build();
        }
        if (!existingListing.getSellerId().equals(sellerId)) {
            return ResponseEntity.status(org.springframework.http.HttpStatus.FORBIDDEN).build();
        }

        try {
            BigDecimal finalPrice = body.get("finalPrice");
            Listing result = listingService.markWon(id, finalPrice);
            return ResponseEntity.ok(ListingPresentation.forListResponse(result));
        } catch (IllegalStateException exception) {
            return ResponseEntity.status(409).body(Map.of("message", exception.getMessage()));
        }
    }

    @PostMapping("/{id}/unsold")
    public ResponseEntity<?> markUnsold(@PathVariable String id, @RequestHeader("X-User-Id") String sellerId) {
        Listing existingListing = listingService.getListingById(id);
        if (existingListing == null) {
            return ResponseEntity.notFound().build();
        }
        if (!existingListing.getSellerId().equals(sellerId)) {
            return ResponseEntity.status(org.springframework.http.HttpStatus.FORBIDDEN).build();
        }

        try {
            Listing result = listingService.markUnsold(id);
            return ResponseEntity.ok(ListingPresentation.forListResponse(result));
        } catch (IllegalStateException exception) {
            return ResponseEntity.status(409).body(Map.of("message", exception.getMessage()));
        }
    }
    @PostMapping("/{id}/admin/close")
    public ResponseEntity<?> adminClose(
            @PathVariable String id,
            @RequestHeader(value = "X-User-Roles", required = false) String rolesHeader,
            @RequestBody(required = false) AdminListingActionRequest request
    ) {
        catalogAccessPolicy.requireAdmin(rolesHeader);
        Listing existingListing = listingService.getListingById(id);
        if (existingListing == null) {
            return ResponseEntity.notFound().build();
        }
        try {
            Listing closed = listingService.adminCloseListing(id);
            String reason = request == null ? null : request.reason();
            listingEventPublisher.publishListingClosedByAdmin(new ListingClosedByAdminEvent(
                    closed.getId(),
                    closed.getSellerId(),
                    closed.getStatus(),
                    reason
            ));
            return ResponseEntity.ok(ListingPresentation.forListResponse(closed));
        } catch (IllegalStateException exception) {
            return ResponseEntity.status(409).body(Map.of("message", exception.getMessage()));
        }
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<?> cancel(@PathVariable String id, @RequestHeader("X-User-Id") String sellerId) {
        Listing existingListing = listingService.getListingById(id);
        if (existingListing == null) {
            return ResponseEntity.notFound().build();
        }
        if (!existingListing.getSellerId().equals(sellerId)) {
            return ResponseEntity.status(org.springframework.http.HttpStatus.FORBIDDEN).build();
        }

        try {
            Listing result = listingService.cancelListing(id);
            return ResponseEntity.ok(ListingPresentation.forListResponse(result));
        } catch (IllegalStateException exception) {
            return ResponseEntity.status(409).body(Map.of("message", exception.getMessage()));
        }
    }
}
