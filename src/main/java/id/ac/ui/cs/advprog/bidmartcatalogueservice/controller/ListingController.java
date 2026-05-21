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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
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
            return ResponseEntity.ok(created);
        } catch (IllegalArgumentException exception) {
            return ResponseEntity.badRequest().body(Map.of("message", exception.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<List<Listing>> getAll() {
        return ResponseEntity.ok(listingService.getAllListings());
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
    public ResponseEntity<Page<Listing>> search(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) ListingStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "title") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        return ResponseEntity.ok(listingService.searchListings(category, keyword, minPrice, maxPrice, status, pageable));
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
            return ResponseEntity.ok(updatedListing);
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
            return ResponseEntity.ok(updated);
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
            return ResponseEntity.ok(published);
        } catch (IllegalStateException exception) {
            return ResponseEntity.status(409).body(Map.of("message", exception.getMessage()));
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
            return ResponseEntity.ok(deactivated);
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
            return ResponseEntity.ok(result);
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
            return ResponseEntity.ok(result);
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
            return ResponseEntity.ok(result);
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
            return ResponseEntity.ok(result);
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
            return ResponseEntity.ok(closed);
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
            return ResponseEntity.ok(result);
        } catch (IllegalStateException exception) {
            return ResponseEntity.status(409).body(Map.of("message", exception.getMessage()));
        }
    }

    @PostMapping("/{id}/bid")
    public ResponseEntity<?> bidPlaced(@PathVariable String id, @RequestBody BidPlacedEvent event) {
        try {
            Listing updated = listingService.handleBidPlaced(id, event.amount());
            if (updated == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(updated);
        } catch (IllegalStateException exception) {
            return ResponseEntity.status(409).body(Map.of("message", exception.getMessage()));
        }
    }

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
            return ResponseEntity.ok(published);
        } catch (IllegalStateException exception) {
            return ResponseEntity.status(409).body(Map.of("message", exception.getMessage()));
        }
    }

    @PostMapping("/{id}/auction-created")
    public ResponseEntity<?> auctionCreated(@PathVariable String id, @RequestHeader("X-User-Id") String sellerId) {
        Listing existingListing = listingService.getListingById(id);
        if (existingListing == null) {
            return ResponseEntity.notFound().build();
        }
        if (!existingListing.getSellerId().equals(sellerId)) {
            return ResponseEntity.status(org.springframework.http.HttpStatus.FORBIDDEN).build();
        }

        try {
            Listing result = listingService.markAuctionCreated(id);
            return ResponseEntity.ok(result);
        } catch (IllegalStateException exception) {
            return ResponseEntity.status(409).body(Map.of("message", exception.getMessage()));
        }
    }

    @PostMapping("/{id}/sold")
    public ResponseEntity<?> markSold(@PathVariable String id, @RequestHeader("X-User-Id") String sellerId, @RequestBody Map<String, BigDecimal> body) {
        Listing existingListing = listingService.getListingById(id);
        if (existingListing == null) {
            return ResponseEntity.notFound().build();
        }
        if (!existingListing.getSellerId().equals(sellerId)) {
            return ResponseEntity.status(org.springframework.http.HttpStatus.FORBIDDEN).build();
        }

        try {
            BigDecimal finalPrice = body.get("finalPrice");
            Listing result = listingService.markSold(id, finalPrice);
            return ResponseEntity.ok(result);
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
            return ResponseEntity.ok(result);
        } catch (IllegalStateException exception) {
            return ResponseEntity.status(409).body(Map.of("message", exception.getMessage()));
        }
    }
}
