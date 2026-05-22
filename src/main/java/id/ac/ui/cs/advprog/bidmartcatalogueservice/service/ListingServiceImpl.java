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
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ListingServiceImpl implements ListingService {

    private final ListingRepository listingRepository;
    private final CategoryRepository categoryRepository;
    private final CategoryService categoryService;

    public ListingServiceImpl(
            ListingRepository listingRepository,
            CategoryRepository categoryRepository,
            CategoryService categoryService
    ) {
        this.listingRepository = listingRepository;
        this.categoryRepository = categoryRepository;
        this.categoryService = categoryService;
    }

    @Override
    public Listing createListing(Listing listing) {
        ListingStatus requestedStatus = listing.getStatus();
        if (listing.getStatus() == null) {
            listing.setStatus(ListingStatus.DRAFT);
        }
        normalizeFinancials(listing);
        validateImageUrl(listing.getImageUrl());
        validateFinancials(listing);
        if (requestedStatus != ListingStatus.DRAFT && hasSchedule(listing)) {
            validateAuctionSchedule(listing);
        }
        resolveCategory(listing);
        if (listing.getMinimumIncrement() == null) {
            listing.setMinimumIncrement(BigDecimal.ONE);
        }
        if (listing.getCurrentPrice() == null && listing.getStartingPrice() != null) {
            listing.setCurrentPrice(listing.getStartingPrice());
        }
        if (listing.getReservePrice() == null && listing.getStartingPrice() != null) {
            listing.setReservePrice(listing.getStartingPrice());
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
    public List<Listing> getListingsBySeller(String sellerId) {
        return listingRepository.findBySellerId(sellerId);
    }

    @Override
    public Page<Listing> searchListings(
            String category,
            Long categoryId,
            String keyword,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            ListingStatus status,
            LocalDateTime endBefore,
            LocalDateTime endAfter,
            Pageable pageable
    ) {
        List<ListingStatus> publicStatuses = status == null
                ? List.of(ListingStatus.ACTIVE, ListingStatus.EXTENDED)
                : List.of();
        List<Long> categoryIds = categoryId == null ? List.of() : categoryService.collectDescendantCategoryIds(categoryId);
        Specification<Listing> spec = ListingSpecification.filterListings(
                keyword,
                category,
                categoryIds,
                minPrice,
                maxPrice,
                status,
                publicStatuses,
                endBefore,
                endAfter
        );
        return listingRepository.findAll(spec, pageable);
    }

    @Override
    public Listing updateListing(String id, Listing listing) {
        return listingRepository.findById(id).map(existingListing -> {
            if (existingListing.isHasBids()) {
                throw new IllegalStateException("Cannot update listing with active bids");
            }
            if (existingListing.getStatus() != ListingStatus.DRAFT) {
                if (existingListing.getStatus() == ListingStatus.ACTIVE
                        || existingListing.getStatus() == ListingStatus.EXTENDED) {
                    validateImageUrl(listing.getImageUrl());
                    existingListing.setDescription(listing.getDescription());
                    existingListing.setImageUrl(listing.getImageUrl());
                    return listingRepository.save(existingListing);
                }
                throw new IllegalStateException("Cannot update listing with status: " + existingListing.getStatus());
            }
            normalizeFinancials(listing);
            validateImageUrl(listing.getImageUrl());
            validateFinancials(listing);
            if (shouldValidateSchedule(listing)) {
                validateAuctionSchedule(listing);
            }
            resolveCategory(listing);
            existingListing.setTitle(listing.getTitle());
            existingListing.setDescription(listing.getDescription());
            existingListing.setImageUrl(listing.getImageUrl());
            existingListing.setStartingPrice(listing.getStartingPrice());
            existingListing.setReservePrice(listing.getReservePrice());
            existingListing.setMinimumIncrement(listing.getMinimumIncrement());
            existingListing.setCurrentPrice(listing.getCurrentPrice());
            existingListing.setStartTime(listing.getStartTime());
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
        return synchronizeBidState(listingId, newPrice, null, null);
    }

    @Override
    public Listing synchronizeBidState(String listingId, BigDecimal newPrice, ListingStatus status, LocalDateTime endTime) {
        return listingRepository.findById(listingId).map(existingListing -> {
            if (existingListing.getStatus() != ListingStatus.ACTIVE && existingListing.getStatus() != ListingStatus.EXTENDED) {
                throw new IllegalStateException("Cannot place bid on listing with status: " + existingListing.getStatus());
            }
            existingListing.setHasBids(true);
            existingListing.setCurrentPrice(newPrice);
            if (status == ListingStatus.ACTIVE || status == ListingStatus.EXTENDED) {
                existingListing.setStatus(status);
            }
            if (endTime != null) {
                existingListing.setEndTime(endTime);
            }
            return listingRepository.save(existingListing);
        }).orElse(null);
    }

    @Override
    public Listing publishListing(String id) {
        return listingRepository.findById(id).map(existingListing -> {
            LocalDateTime now = LocalDateTime.now();
            if (existingListing.getStatus() != ListingStatus.DRAFT) {
                throw new IllegalStateException("Only DRAFT listings can be published, current status: " + existingListing.getStatus());
            }
            if (existingListing.getEndTime() == null) {
                throw new IllegalStateException("Auction end time is required before publishing");
            }
            if (existingListing.getStartingPrice() == null) {
                throw new IllegalStateException("Starting price is required before publishing");
            }
            if (existingListing.getReservePrice() == null) {
                existingListing.setReservePrice(existingListing.getStartingPrice());
            }
            if (existingListing.getReservePrice().compareTo(existingListing.getStartingPrice()) < 0) {
                throw new IllegalStateException("Reserve price must be greater than or equal to starting price");
            }
            if (existingListing.getMinimumIncrement() == null) {
                existingListing.setMinimumIncrement(BigDecimal.ONE);
            }
            existingListing.setStartTime(now);
            if (existingListing.getCurrentPrice() == null) {
                existingListing.setCurrentPrice(existingListing.getStartingPrice());
            }
            normalizeFinancials(existingListing);
            validateAuctionSchedule(existingListing, now);
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
    public Listing markExtended(String id) {
        return listingRepository.findById(id).map(existingListing -> {
            if (existingListing.getStatus() != ListingStatus.ACTIVE && existingListing.getStatus() != ListingStatus.EXTENDED) {
                throw new IllegalStateException("Only ACTIVE or EXTENDED listings can be marked as EXTENDED, current status: " + existingListing.getStatus());
            }
            existingListing.setStatus(ListingStatus.EXTENDED);
            return listingRepository.save(existingListing);
        }).orElse(null);
    }

    @Override
    public Listing markClosed(String id) {
        return listingRepository.findById(id).map(existingListing -> {
            if (existingListing.getStatus() != ListingStatus.ACTIVE && existingListing.getStatus() != ListingStatus.EXTENDED) {
                throw new IllegalStateException("Only ACTIVE or EXTENDED listings can be marked as CLOSED, current status: " + existingListing.getStatus());
            }
            existingListing.setStatus(ListingStatus.CLOSED);
            return listingRepository.save(existingListing);
        }).orElse(null);
    }

    @Override
    public Listing markWon(String id, BigDecimal finalPrice) {
        return listingRepository.findById(id).map(existingListing -> {
            if (existingListing.getStatus() != ListingStatus.ACTIVE
                    && existingListing.getStatus() != ListingStatus.EXTENDED
                    && existingListing.getStatus() != ListingStatus.CLOSED) {
                throw new IllegalStateException("Only ACTIVE, EXTENDED, or CLOSED listings can be marked as WON, current status: " + existingListing.getStatus());
            }
            existingListing.setStatus(ListingStatus.WON);
            existingListing.setCurrentPrice(finalPrice);
            return listingRepository.save(existingListing);
        }).orElse(null);
    }

    @Override
    public Listing markUnsold(String id) {
        return listingRepository.findById(id).map(existingListing -> {
            if (existingListing.getStatus() != ListingStatus.ACTIVE
                    && existingListing.getStatus() != ListingStatus.EXTENDED
                    && existingListing.getStatus() != ListingStatus.CLOSED) {
                throw new IllegalStateException("Only ACTIVE, EXTENDED, or CLOSED listings can be marked as UNSOLD, current status: " + existingListing.getStatus());
            }
            existingListing.setStatus(ListingStatus.UNSOLD);
            return listingRepository.save(existingListing);
        }).orElse(null);
    }

    @Override
    public Listing adminCloseListing(String id) {
        return listingRepository.findById(id).map(existingListing -> {
            if (existingListing.getStatus() == ListingStatus.CANCELLED
                    || existingListing.getStatus() == ListingStatus.WON
                    || existingListing.getStatus() == ListingStatus.UNSOLD) {
                throw new IllegalStateException("Cannot admin-close listing with status: " + existingListing.getStatus());
            }
            existingListing.setStatus(ListingStatus.CANCELLED);
            return listingRepository.save(existingListing);
        }).orElse(null);
    }

    @Override
    public Listing cancelListing(String id) {
        return listingRepository.findById(id).map(existingListing -> {
            if (existingListing.isHasBids()) {
                throw new IllegalStateException("Listing has active bids");
            }
            if (existingListing.getStatus() == ListingStatus.WON ||
                existingListing.getStatus() == ListingStatus.UNSOLD || 
                existingListing.getStatus() == ListingStatus.CLOSED ||
                existingListing.getStatus() == ListingStatus.CANCELLED) {
                throw new IllegalStateException("Cannot cancel listing with status: " + existingListing.getStatus());
            }
            existingListing.setStatus(ListingStatus.CANCELLED);
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

    private void validateFinancials(Listing listing) {
        BigDecimal startingPrice = listing.getStartingPrice();
        BigDecimal reservePrice = listing.getReservePrice();
        BigDecimal minimumIncrement = listing.getMinimumIncrement();

        if (startingPrice != null && startingPrice.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Starting price must be greater than 0");
        }
        if (reservePrice != null && startingPrice != null && reservePrice.compareTo(startingPrice) < 0) {
            throw new IllegalArgumentException("Reserve price must be greater than or equal to starting price");
        }
        if (minimumIncrement != null && minimumIncrement.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Minimum increment must be greater than 0");
        }
    }

    private void normalizeFinancials(Listing listing) {
        if (listing.getStartingPrice() != null) {
            listing.setStartingPrice(listing.getStartingPrice().setScale(0, RoundingMode.HALF_UP));
        }
        if (listing.getReservePrice() != null) {
            listing.setReservePrice(listing.getReservePrice().setScale(0, RoundingMode.HALF_UP));
        }
        if (listing.getCurrentPrice() != null) {
            listing.setCurrentPrice(listing.getCurrentPrice().setScale(0, RoundingMode.HALF_UP));
        }
        if (listing.getMinimumIncrement() != null) {
            // Currency increment in IDR should remain whole-rupiah to avoid float drift from clients.
            listing.setMinimumIncrement(listing.getMinimumIncrement().setScale(0, RoundingMode.HALF_UP));
        }
    }

    private void validateAuctionSchedule(Listing listing) {
        validateAuctionSchedule(listing, LocalDateTime.now());
    }

    private boolean shouldValidateSchedule(Listing listing) {
        return listing.getStatus() != null && listing.getStatus() != ListingStatus.DRAFT;
    }

    private boolean hasSchedule(Listing listing) {
        return listing.getStartTime() != null || listing.getEndTime() != null;
    }

    private void validateAuctionSchedule(Listing listing, LocalDateTime now) {
        LocalDateTime startTime = listing.getStartTime();
        LocalDateTime endTime = listing.getEndTime();

        if (startTime != null && startTime.isBefore(now)) {
            throw new IllegalArgumentException("Start time must be greater than or equal to current time");
        }

        if (endTime != null && !endTime.isAfter(now)) {
            throw new IllegalArgumentException("End time must be in the future");
        }

        if (startTime != null && endTime != null && !endTime.isAfter(startTime)) {
            throw new IllegalArgumentException("End time must be after start time");
        }
    }
}
