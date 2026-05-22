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
            listing.setImageUrl(EMBEDDED_IMAGE_PLACEHOLDER);
        }
        return listing;
    }

    public static List<Listing> forListResponse(List<Listing> listings) {
        if (listings == null) {
            return List.of();
        }
        return listings.stream().map(ListingPresentation::forListResponse).toList();
    }

    public static Page<Listing> forListResponse(Page<Listing> page) {
        return page.map(ListingPresentation::forListResponse);
    }
}
