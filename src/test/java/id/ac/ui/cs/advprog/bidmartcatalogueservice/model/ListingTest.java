package id.ac.ui.cs.advprog.bidmartcatalogueservice.model;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ListingTest {

    @Test
    void testListingCreationWithBuilder() {
        LocalDateTime endTime = LocalDateTime.now().plusDays(3);

        Listing listing = Listing.builder()
                .id("LST-001")
                .sellerId("USR-123")
                .title("Sepeda Gunung")
                .description("Kondisi mulus 99%")
                .category("Olahraga")
                .startingPrice(new BigDecimal("1500000"))
                .currentPrice(new BigDecimal("1500000"))
                .status(ListingStatus.ACTIVE)
                .endTime(endTime)
                .build();

        assertNotNull(listing);
        assertEquals("LST-001", listing.getId());
        assertEquals("USR-123", listing.getSellerId());
        assertEquals("Sepeda Gunung", listing.getTitle());
        assertEquals("Kondisi mulus 99%", listing.getDescription());
        assertEquals(new BigDecimal("1500000"), listing.getStartingPrice());
        assertEquals(new BigDecimal("1500000"), listing.getCurrentPrice());
        assertEquals(ListingStatus.ACTIVE, listing.getStatus());
        assertEquals(endTime, listing.getEndTime());
    }

    @Test
    void testListingSetters() {
        Listing listing = new Listing();
        listing.setTitle("Laptop Bekas");
        listing.setStatus(ListingStatus.DRAFT);

        assertEquals("Laptop Bekas", listing.getTitle());
        assertEquals(ListingStatus.DRAFT, listing.getStatus());
    }

    @Test
    void testAllListingStatusValues() {
        Listing listing = new Listing();

        listing.setStatus(ListingStatus.DRAFT);
        assertEquals(ListingStatus.DRAFT, listing.getStatus());

        listing.setStatus(ListingStatus.ACTIVE);
        assertEquals(ListingStatus.ACTIVE, listing.getStatus());

        listing.setStatus(ListingStatus.EXTENDED);
        assertEquals(ListingStatus.EXTENDED, listing.getStatus());

        listing.setStatus(ListingStatus.CLOSED);
        assertEquals(ListingStatus.CLOSED, listing.getStatus());

        listing.setStatus(ListingStatus.WON);
        assertEquals(ListingStatus.WON, listing.getStatus());

        listing.setStatus(ListingStatus.UNSOLD);
        assertEquals(ListingStatus.UNSOLD, listing.getStatus());

        listing.setStatus(ListingStatus.CANCELLED);
        assertEquals(ListingStatus.CANCELLED, listing.getStatus());
    }
}
