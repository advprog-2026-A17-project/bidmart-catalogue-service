package id.ac.ui.cs.advprog.bidmartcatalogueservice.service;

import id.ac.ui.cs.advprog.bidmartcatalogueservice.model.Listing;
import id.ac.ui.cs.advprog.bidmartcatalogueservice.repository.ListingRepository;
import id.ac.ui.cs.advprog.bidmartcatalogueservice.specification.ListingSpecification;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class ListingServiceImpl implements ListingService {

    private final ListingRepository listingRepository;

    public ListingServiceImpl(ListingRepository listingRepository) {
        this.listingRepository = listingRepository;
    }

    @Override
    public Listing createListing(Listing listing) {
        if (listing.getStatus() == null) {
            listing.setStatus("ACTIVE");
        }
        return listingRepository.save(listing);
    }

    @Override
    public Listing getListingById(String id) {
        return listingRepository.findById(id).orElse(null);
    }
    @Override
    public List<Listing> getAllListings() {
        return listingRepository.findAll();
    }

    @Override
    public List<Listing> searchListings(String category, String keyword, BigDecimal minPrice, BigDecimal maxPrice, String status) {
        Specification<Listing> spec = ListingSpecification.filterListings(keyword, category, minPrice, maxPrice, status);
        return listingRepository.findAll(spec);
    }

    @Override
    public Listing updateListing(String id, Listing listing) {
        return listingRepository.findById(id).map(existingListing -> {
            if (existingListing.isHasBids()) {
                throw new IllegalStateException("Cannot update listing with active bids");
            }
            existingListing.setTitle(listing.getTitle());
            existingListing.setDescription(listing.getDescription());
            existingListing.setImageUrl(listing.getImageUrl());
            existingListing.setStartingPrice(listing.getStartingPrice());
            existingListing.setCurrentPrice(listing.getCurrentPrice());
            existingListing.setEndTime(listing.getEndTime());
            existingListing.setSellerId(listing.getSellerId());
            existingListing.setCategory(listing.getCategory());
            return listingRepository.save(existingListing);
        }).orElse(null);
    }

    @Override
    public Listing cancelListing(String id) {
        return listingRepository.findById(id).map(existingListing -> {
            if (existingListing.isHasBids()) {
                throw new IllegalStateException("Listing has active bids");
            }
            existingListing.setStatus("CANCELLED");
            return listingRepository.save(existingListing);
        }).orElse(null);
    }

    @Override
    public void deleteListing(String id) {
        listingRepository.deleteById(id);
    }

    @Override
    public Listing handleBidPlaced(String listingId, BigDecimal newPrice) {
        return listingRepository.findById(listingId).map(existingListing -> {
            existingListing.setHasBids(true);
            existingListing.setCurrentPrice(newPrice);
            return listingRepository.save(existingListing);
        }).orElse(null);
    }
}
