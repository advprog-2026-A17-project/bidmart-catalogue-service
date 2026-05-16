package id.ac.ui.cs.advprog.bidmartcatalogueservice.service;

import id.ac.ui.cs.advprog.bidmartcatalogueservice.model.Category;
import id.ac.ui.cs.advprog.bidmartcatalogueservice.model.Listing;
import id.ac.ui.cs.advprog.bidmartcatalogueservice.model.ListingStatus;
import id.ac.ui.cs.advprog.bidmartcatalogueservice.repository.CategoryRepository;
import id.ac.ui.cs.advprog.bidmartcatalogueservice.repository.ListingRepository;
import id.ac.ui.cs.advprog.bidmartcatalogueservice.specification.ListingSpecification;
import id.ac.ui.cs.advprog.bidmartcatalogueservice.util.ImageUrlValidator;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class ListingServiceImpl implements ListingService {

    private final ListingRepository listingRepository;
    private final CategoryRepository categoryRepository;

    public ListingServiceImpl(ListingRepository listingRepository, CategoryRepository categoryRepository) {
        this.listingRepository = listingRepository;
        this.categoryRepository = categoryRepository;
    }

    @Override
    public Listing createListing(Listing listing) {
        validateImageUrl(listing.getImageUrl());
        resolveCategory(listing);
        if (listing.getStatus() == null) {
            listing.setStatus(ListingStatus.DRAFT);
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
    public Page<Listing> searchListings(String category, String keyword, BigDecimal minPrice, BigDecimal maxPrice, ListingStatus status, Pageable pageable) {
        Specification<Listing> spec = ListingSpecification.filterListings(keyword, category, minPrice, maxPrice, status);
        return listingRepository.findAll(spec, pageable);
    }

    @Override
    public Listing updateListing(String id, Listing listing) {
        return listingRepository.findById(id).map(existingListing -> {
            if (existingListing.isHasBids()) {
                throw new IllegalStateException("Cannot update listing with active bids");
            }
            if (existingListing.getStatus() != ListingStatus.DRAFT && existingListing.getStatus() != ListingStatus.ACTIVE) {
                throw new IllegalStateException("Cannot update listing with status: " + existingListing.getStatus());
            }
            validateImageUrl(listing.getImageUrl());
            resolveCategory(listing);
            existingListing.setTitle(listing.getTitle());
            existingListing.setDescription(listing.getDescription());
            existingListing.setImageUrl(listing.getImageUrl());
            existingListing.setStartingPrice(listing.getStartingPrice());
            existingListing.setCurrentPrice(listing.getCurrentPrice());
            existingListing.setEndTime(listing.getEndTime());
            existingListing.setSellerId(listing.getSellerId());
            existingListing.setCategory(listing.getCategory());
            existingListing.setCondition(listing.getCondition());
            existingListing.setCategoryEntity(listing.getCategoryEntity());
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
            if (existingListing.getStatus() != ListingStatus.ACTIVE && existingListing.getStatus() != ListingStatus.AUCTION_CREATED) {
                throw new IllegalStateException("Cannot place bid on listing with status: " + existingListing.getStatus());
            }
            existingListing.setHasBids(true);
            existingListing.setCurrentPrice(newPrice);
            return listingRepository.save(existingListing);
        }).orElse(null);
    }

    @Override
    public Listing publishListing(String id) {
        return listingRepository.findById(id).map(existingListing -> {
            if (existingListing.getStatus() != ListingStatus.DRAFT) {
                throw new IllegalStateException("Only DRAFT listings can be published, current status: " + existingListing.getStatus());
            }
            existingListing.setStatus(ListingStatus.ACTIVE);
            return listingRepository.save(existingListing);
        }).orElse(null);
    }

    @Override
    public Listing deactivateListing(String id) {
        return listingRepository.findById(id).map(existingListing -> {
            if (existingListing.isHasBids()) {
                throw new IllegalStateException("Cannot deactivate listing with active bids");
            }
            if (existingListing.getStatus() != ListingStatus.ACTIVE) {
                throw new IllegalStateException("Only ACTIVE listings can be deactivated, current status: " + existingListing.getStatus());
            }
            existingListing.setStatus(ListingStatus.DRAFT);
            return listingRepository.save(existingListing);
        }).orElse(null);
    }

    @Override
    public Listing markAuctionCreated(String id) {
        return listingRepository.findById(id).map(existingListing -> {
            if (existingListing.getStatus() != ListingStatus.ACTIVE) {
                throw new IllegalStateException("Only ACTIVE listings can be marked as AUCTION_CREATED, current status: " + existingListing.getStatus());
            }
            existingListing.setStatus(ListingStatus.AUCTION_CREATED);
            return listingRepository.save(existingListing);
        }).orElse(null);
    }

    @Override
    public Listing markSold(String id, BigDecimal finalPrice) {
        return listingRepository.findById(id).map(existingListing -> {
            if (existingListing.getStatus() != ListingStatus.AUCTION_CREATED) {
                throw new IllegalStateException("Only AUCTION_CREATED listings can be marked as SOLD, current status: " + existingListing.getStatus());
            }
            existingListing.setStatus(ListingStatus.SOLD);
            existingListing.setCurrentPrice(finalPrice);
            return listingRepository.save(existingListing);
        }).orElse(null);
    }

    @Override
    public Listing markUnsold(String id) {
        return listingRepository.findById(id).map(existingListing -> {
            if (existingListing.getStatus() != ListingStatus.AUCTION_CREATED) {
                throw new IllegalStateException("Only AUCTION_CREATED listings can be marked as UNSOLD, current status: " + existingListing.getStatus());
            }
            existingListing.setStatus(ListingStatus.UNSOLD);
            return listingRepository.save(existingListing);
        }).orElse(null);
    }

    private void resolveCategory(Listing listing) {
        if (listing.getCategoryEntity() != null && listing.getCategoryEntity().getId() != null) {
            Category category = categoryRepository.findById(listing.getCategoryEntity().getId())
                    .orElseThrow(() -> new IllegalArgumentException("Category not found with id: " + listing.getCategoryEntity().getId()));
            listing.setCategoryEntity(category);
            listing.setCategory(category.getName());
        }
    }

    private void validateImageUrl(String imageUrl) {
        if (!ImageUrlValidator.isValidImageUrl(imageUrl)) {
            throw new IllegalArgumentException("Invalid image URL format: " + imageUrl);
        }
    }
}
