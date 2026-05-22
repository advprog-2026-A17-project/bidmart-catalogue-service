package id.ac.ui.cs.advprog.bidmartcatalogueservice.service;

import id.ac.ui.cs.advprog.bidmartcatalogueservice.model.Listing;
import id.ac.ui.cs.advprog.bidmartcatalogueservice.model.ListingStatus;
import id.ac.ui.cs.advprog.bidmartcatalogueservice.repository.CategoryRepository;
import id.ac.ui.cs.advprog.bidmartcatalogueservice.repository.ListingRepository;
import id.ac.ui.cs.advprog.bidmartcatalogueservice.service.listing.ListingExpiryStrategy;
import id.ac.ui.cs.advprog.bidmartcatalogueservice.service.listing.ListingStates;
import id.ac.ui.cs.advprog.bidmartcatalogueservice.service.listing.ListingValidationChain;
import id.ac.ui.cs.advprog.bidmartcatalogueservice.service.listing.ListingValidationContext;
import id.ac.ui.cs.advprog.bidmartcatalogueservice.specification.ListingSpecification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ListingServiceImpl implements ListingService {

    private final ListingRepository listingRepository;
    private final CategoryRepository categoryRepository;
    private final CategoryService categoryService;
    private final ListingValidationChain validationChain = ListingValidationChain.defaultChain();

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
        if (listing.getStatus() == null) {
            listing.setStatus(ListingStatus.DRAFT);
        }
        validationChain.validate(new ListingValidationContext(
                listing,
                null,
                categoryRepository,
                LocalDateTime.now(),
                listing.getStatus() != ListingStatus.DRAFT && hasSchedule(listing),
                true
        ));
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
        return listingRepository.findById(id)
                .map(this::reconcileExpiredPublishedListing)
                .orElse(null);
    }

    @Override
    public List<Listing> getAllListings() {
        return listingRepository.findAll().stream()
                .map(this::reconcileExpiredPublishedListing)
                .toList();
    }

    @Override
    public List<Listing> getListingsBySeller(String sellerId) {
        return listingRepository.findBySellerId(sellerId).stream()
                .map(this::reconcileExpiredPublishedListing)
                .toList();
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
        return listingRepository.findAll(spec, pageable)
                .map(this::reconcileExpiredPublishedListing);
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
                    resolveImageUrlForUpdate(existingListing, listing);
                    existingListing.setDescription(listing.getDescription());
                    existingListing.setImageUrl(listing.getImageUrl());
                    return listingRepository.save(existingListing);
                }
                throw new IllegalStateException("Cannot update listing with status: " + existingListing.getStatus());
            }
            ListingValidationChain.normalizeFinancials(listing);
            ListingValidationChain.resolveImageUrlForUpdate(existingListing, listing);
            validationChain.validate(new ListingValidationContext(
                    listing,
                    existingListing,
                    categoryRepository,
                    LocalDateTime.now(),
                    shouldValidateSchedule(listing),
                    true
            ));
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
            if (!canSynchronizeBidState(existingListing.getStatus())) {
                throw new IllegalStateException("Cannot place bid on listing with status: " + existingListing.getStatus());
            }
            existingListing.setHasBids(true);
            existingListing.setCurrentPrice(newPrice);
            if (isPublishedListing(existingListing.getStatus())
                    && (status == ListingStatus.ACTIVE || status == ListingStatus.EXTENDED)) {
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
            ListingValidationChain.normalizeFinancials(existingListing);
            ListingValidationChain.validateAuctionSchedule(existingListing, now);
            ListingStates.forStatus(existingListing.getStatus()).publish(existingListing);
            return listingRepository.save(existingListing);
        }).orElse(null);
    }

    @Override
    public Listing deactivateListing(String id) {
        return listingRepository.findById(id).map(existingListing -> {
            ListingStates.forStatus(existingListing.getStatus()).deactivate(existingListing);
            return listingRepository.save(existingListing);
        }).orElse(null);
    }

    @Override
    public Listing markExtended(String id) {
        return listingRepository.findById(id).map(existingListing -> {
            ListingStates.forStatus(existingListing.getStatus()).markExtended(existingListing);
            return listingRepository.save(existingListing);
        }).orElse(null);
    }

    @Override
    public Listing markClosed(String id) {
        return listingRepository.findById(id).map(existingListing -> {
            ListingStates.forStatus(existingListing.getStatus()).markClosed(existingListing);
            return listingRepository.save(existingListing);
        }).orElse(null);
    }

    @Override
    public Listing markWon(String id, BigDecimal finalPrice) {
        return listingRepository.findById(id).map(existingListing -> {
            ListingStates.forStatus(existingListing.getStatus()).markWon(existingListing, finalPrice);
            return listingRepository.save(existingListing);
        }).orElse(null);
    }

    @Override
    public Listing markUnsold(String id) {
        return listingRepository.findById(id).map(existingListing -> {
            ListingStates.forStatus(existingListing.getStatus()).markUnsold(existingListing);
            return listingRepository.save(existingListing);
        }).orElse(null);
    }

    @Override
    public Listing adminCloseListing(String id) {
        return listingRepository.findById(id).map(existingListing -> {
            ListingStates.forStatus(existingListing.getStatus()).adminClose(existingListing);
            return listingRepository.save(existingListing);
        }).orElse(null);
    }

    @Override
    public Listing cancelListing(String id) {
        return listingRepository.findById(id).map(existingListing -> {
            ListingStates.forStatus(existingListing.getStatus()).cancel(existingListing);
            return listingRepository.save(existingListing);
        }).orElse(null);
    }

    private void resolveCategory(Listing listing) {
        validationChain.validate(new ListingValidationContext(
                listing,
                null,
                categoryRepository,
                LocalDateTime.now(),
                false,
                true
        ));
    }

    private Listing reconcileExpiredPublishedListing(Listing listing) {
        if (listing == null || !isPublishedListing(listing.getStatus()) || listing.getEndTime() == null) {
            return listing;
        }
        if (listing.getEndTime().isAfter(LocalDateTime.now())) {
            return listing;
        }
        listing.setStatus(ListingExpiryStrategy.forListing(listing).resolveExpiredStatus(listing));
        return listingRepository.save(listing);
    }

    private ListingStatus resolveExpiredStatus(Listing listing) {
        return ListingExpiryStrategy.forListing(listing).resolveExpiredStatus(listing);
    }

    private boolean isPublishedListing(ListingStatus status) {
        return status == ListingStatus.ACTIVE || status == ListingStatus.EXTENDED;
    }

    private boolean canSynchronizeBidState(ListingStatus status) {
        return status == ListingStatus.ACTIVE
                || status == ListingStatus.EXTENDED
                || status == ListingStatus.CLOSED
                || status == ListingStatus.UNSOLD;
    }

    private void validateImageUrl(String imageUrl) {
        ListingValidationChain.validateImageUrl(imageUrl);
    }

    private void resolveImageUrlForUpdate(Listing existingListing, Listing incomingListing) {
        ListingValidationChain.resolveImageUrlForUpdate(existingListing, incomingListing);
    }

    private void validateFinancials(Listing listing) {
        ListingValidationChain.validateFinancials(listing);
    }

    private void normalizeFinancials(Listing listing) {
        ListingValidationChain.normalizeFinancials(listing);
    }

    private void validateAuctionSchedule(Listing listing) {
        ListingValidationChain.validateAuctionSchedule(listing, LocalDateTime.now());
    }

    private boolean shouldValidateSchedule(Listing listing) {
        return listing.getStatus() != null && listing.getStatus() != ListingStatus.DRAFT;
    }

    private boolean hasSchedule(Listing listing) {
        return listing.getStartTime() != null || listing.getEndTime() != null;
    }

    private void validateAuctionSchedule(Listing listing, LocalDateTime now) {
        ListingValidationChain.validateAuctionSchedule(listing, now);
    }
}
