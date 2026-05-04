package id.ac.ui.cs.advprog.bidmartcatalogueservice.service;

import id.ac.ui.cs.advprog.bidmartcatalogueservice.model.Listing;

import java.math.BigDecimal;
import java.util.List;

public interface ListingService {
    Listing createListing(Listing listing);
    Listing getListingById(String id);
    List<Listing> getAllListings();
    List<Listing> searchListings(String category, String keyword, BigDecimal minPrice, BigDecimal maxPrice, String status);
    Listing updateListing(String id, Listing listing);
    Listing cancelListing(String id);
    void deleteListing(String id);
    Listing handleBidPlaced(String listingId, java.math.BigDecimal newPrice);
}
