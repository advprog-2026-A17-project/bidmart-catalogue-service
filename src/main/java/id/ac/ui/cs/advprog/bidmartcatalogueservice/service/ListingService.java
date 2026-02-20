package id.ac.ui.cs.advprog.bidmartcatalogueservice.service;

import id.ac.ui.cs.advprog.bidmartcatalogueservice.model.Listing;
import java.util.List;

public interface ListingService {
    Listing createListing(Listing listing);
    Listing getListingById(String id);
    List<Listing> getAllListings();
    List<Listing> searchListings(String category, String keyword);
    Listing updateListing(String id, Listing listing);
    void deleteListing(String id);
}