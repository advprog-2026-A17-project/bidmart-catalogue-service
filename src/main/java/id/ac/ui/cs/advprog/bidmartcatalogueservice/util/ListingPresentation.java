package id.ac.ui.cs.advprog.bidmartcatalogueservice.util;

import id.ac.ui.cs.advprog.bidmartcatalogueservice.model.Listing;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * Avoid shipping large base64 data URLs in list/search API responses (breaks HTTP/2 via Caddy).
 * Full imageUrl remains available on GET /listings/{id}.
 */
public final class ListingPresentation {

    public static final String EMBEDDED_IMAGE_PLACEHOLDER = "embedded://listing-image";

    private ListingPresentation() {
    }

    public static boolean isEmbeddedDataUrl(String imageUrl) {
        return imageUrl != null && imageUrl.regionMatches(true, 0, "data:image/", 0, "data:image/".length());
    }

    public static Listing forListResponse(Listing listing) {
        if (listing != null && isEmbeddedDataUrl(listing.getImageUrl())) {
            return Listing.builder()
                .id(listing.getId())
                .sellerId(listing.getSellerId())
                .title(listing.getTitle())
                .description(listing.getDescription())
                .category(listing.getCategory())
                .condition(listing.getCondition())
                .imageUrl(EMBEDDED_IMAGE_PLACEHOLDER)
                .categoryEntity(null) // Exclude proxy to prevent Jackson introspection errors
                .startingPrice(listing.getStartingPrice())
                .reservePrice(listing.getReservePrice())
                .currentPrice(listing.getCurrentPrice())
                .minimumIncrement(listing.getMinimumIncrement())
                .startTime(listing.getStartTime())
                .endTime(listing.getEndTime())
                .status(listing.getStatus())
                .hasBids(listing.isHasBids())
                .build();
        }
        return listing;
    }

    public static List<Listing> forListResponse(List<Listing> listings) {
        if (listings == null) {
            return java.util.List.of();
        }
        return listings.stream().map(ListingPresentation::forListResponse).collect(java.util.stream.Collectors.toList());
    }

    public static Page<Listing> forListResponse(Page<Listing> page) {
        return page.map(ListingPresentation::forListResponse);
    }
}
