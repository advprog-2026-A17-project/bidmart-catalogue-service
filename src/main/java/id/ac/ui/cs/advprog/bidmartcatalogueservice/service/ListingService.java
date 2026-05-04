package id.ac.ui.cs.advprog.bidmartcatalogueservice.service;

import id.ac.ui.cs.advprog.bidmartcatalogueservice.model.Listing;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;

public interface ListingService {
    Listing createListing(Listing listing);
    Listing getListingById(String id);
    List<Listing> getAllListings();
    Page<Listing> searchListings(String category, String keyword, BigDecimal minPrice, BigDecimal maxPrice, String status, Pageable pageable);
    Listing updateListing(String id, Listing listing);
    Listing cancelListing(String id);
    void deleteListing(String id);
    Listing handleBidPlaced(String listingId, BigDecimal newPrice);
}
