package id.ac.ui.cs.advprog.bidmartcatalogueservice.controller;

import id.ac.ui.cs.advprog.bidmartcatalogueservice.dto.ListingSummaryResponse;
import id.ac.ui.cs.advprog.bidmartcatalogueservice.model.Listing;
import id.ac.ui.cs.advprog.bidmartcatalogueservice.service.ListingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/catalogue/listings")
public class ListingController {

    private final ListingService listingService;

    public ListingController(ListingService listingService) {
        this.listingService = listingService;
    }

    @PostMapping
    public ResponseEntity<Listing> create(@RequestHeader("X-userid") String sellerId, @RequestBody Listing listing) {
        listing.setSellerId(sellerId);
        return ResponseEntity.ok(listingService.createListing(listing));
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
    public ResponseEntity<List<Listing>> search(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) String status) {

        return ResponseEntity.ok(listingService.searchListings(category, keyword, minPrice, maxPrice, status));
    }
    @PutMapping("/{id}")
    public ResponseEntity<Listing> update(@PathVariable String id, @RequestHeader("X-userid") String sellerId, @RequestBody Listing listing) {
        Listing existingListing = listingService.getListingById(id);
        if (existingListing == null) {
            return ResponseEntity.notFound().build();
        }
        if (!existingListing.getSellerId().equals(sellerId)) {
            return ResponseEntity.status(org.springframework.http.HttpStatus.FORBIDDEN).build();
        }
        listing.setSellerId(sellerId);
        Listing updatedListing = listingService.updateListing(id, listing);
        return ResponseEntity.ok(updatedListing);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id, @RequestHeader("X-userid") String sellerId) {
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

    @PostMapping("/{id}/cancel")
    public ResponseEntity<?> cancel(@PathVariable String id) {
        try {
            Listing cancelledListing = listingService.cancelListing(id);
            if (cancelledListing == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(cancelledListing);
        } catch (IllegalStateException exception) {
            return ResponseEntity.status(409).body(Map.of("message", exception.getMessage()));
        }
    }
}
