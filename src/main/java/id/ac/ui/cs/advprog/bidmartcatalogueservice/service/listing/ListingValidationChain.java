package id.ac.ui.cs.advprog.bidmartcatalogueservice.service.listing;

import id.ac.ui.cs.advprog.bidmartcatalogueservice.model.Category;
import id.ac.ui.cs.advprog.bidmartcatalogueservice.model.Listing;
import id.ac.ui.cs.advprog.bidmartcatalogueservice.util.ImageUrlValidator;
import id.ac.ui.cs.advprog.bidmartcatalogueservice.util.ListingPresentation;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

public final class ListingValidationChain {

    private final List<ListingValidationLink> links;

    private ListingValidationChain(List<ListingValidationLink> links) {
        this.links = links;
    }

    public static ListingValidationChain defaultChain() {
        return new ListingValidationChain(List.of(
                new ImageUrlValidationLink(),
                new FinancialNormalizationLink(),
                new FinancialValidationLink(),
                new ScheduleValidationLink(),
                new CategoryResolutionLink()
        ));
    }

    public void validate(ListingValidationContext context) {
        for (ListingValidationLink link : links) {
            link.validate(context);
        }
    }

    public static void resolveImageUrlForUpdate(Listing existingListing, Listing incomingListing) {
        if (ListingPresentation.EMBEDDED_IMAGE_PLACEHOLDER.equals(incomingListing.getImageUrl())) {
            incomingListing.setImageUrl(existingListing.getImageUrl());
        }
        validateImageUrl(incomingListing.getImageUrl());
    }

    public static void normalizeFinancials(Listing listing) {
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
            listing.setMinimumIncrement(listing.getMinimumIncrement().setScale(0, RoundingMode.HALF_UP));
        }
    }

    public static void validateFinancials(Listing listing) {
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

    public static void validateAuctionSchedule(Listing listing, LocalDateTime now) {
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

    public static void validateImageUrl(String imageUrl) {
        if (!ImageUrlValidator.isValidImageUrl(imageUrl)) {
            throw new IllegalArgumentException("Invalid image URL format: " + imageUrl);
        }
    }

    private static final class ImageUrlValidationLink implements ListingValidationLink {
        @Override
        public void validate(ListingValidationContext context) {
            validateImageUrl(context.listing().getImageUrl());
        }
    }

    private static final class FinancialNormalizationLink implements ListingValidationLink {
        @Override
        public void validate(ListingValidationContext context) {
            normalizeFinancials(context.listing());
        }
    }

    private static final class FinancialValidationLink implements ListingValidationLink {
        @Override
        public void validate(ListingValidationContext context) {
            validateFinancials(context.listing());
        }
    }

    private static final class ScheduleValidationLink implements ListingValidationLink {
        @Override
        public void validate(ListingValidationContext context) {
            if (context.validateSchedule()) {
                validateAuctionSchedule(context.listing(), context.now());
            }
        }
    }

    private static final class CategoryResolutionLink implements ListingValidationLink {
        @Override
        public void validate(ListingValidationContext context) {
            Listing listing = context.listing();
            if (!context.resolveCategory()
                    || listing.getCategoryEntity() == null
                    || listing.getCategoryEntity().getId() == null) {
                return;
            }
            Category category = context.categoryRepository()
                    .findById(listing.getCategoryEntity().getId())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Category not found with id: " + listing.getCategoryEntity().getId()));
            listing.setCategoryEntity(category);
            listing.setCategory(category.getName());
        }
    }
}
