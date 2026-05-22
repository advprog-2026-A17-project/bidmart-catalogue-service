package id.ac.ui.cs.advprog.bidmartcatalogueservice.util;

import id.ac.ui.cs.advprog.bidmartcatalogueservice.model.Listing;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ListingPresentationTest {

    @Test
    void forListResponse_replacesEmbeddedDataUrlWithPlaceholder() {
        Listing listing = Listing.builder()
                .id("1")
                .imageUrl("data:image/png;base64,abc")
                .build();

        Listing sanitized = ListingPresentation.forListResponse(listing);

        assertEquals(ListingPresentation.EMBEDDED_IMAGE_PLACEHOLDER, sanitized.getImageUrl());
    }

    @Test
    void forListResponse_keepsHttpsImageUrl() {
        Listing listing = Listing.builder()
                .id("2")
                .imageUrl("https://example.com/photo.jpg")
                .build();

        Listing sanitized = ListingPresentation.forListResponse(listing);

        assertEquals("https://example.com/photo.jpg", sanitized.getImageUrl());
    }
}
