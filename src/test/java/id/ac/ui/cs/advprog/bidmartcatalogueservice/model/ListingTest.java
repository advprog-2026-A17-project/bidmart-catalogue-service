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
                .status("ACTIVE")
                .endTime(endTime)
                .build();

        assertNotNull(listing);
        assertEquals("LST-001", listing.getId());
        assertEquals("USR-123", listing.getSellerId());
        assertEquals("Sepeda Gunung", listing.getTitle());
        assertEquals("Kondisi mulus 99%", listing.getDescription());
        assertEquals(new BigDecimal("1500000"), listing.getStartingPrice());
        assertEquals(new BigDecimal("1500000"), listing.getCurrentPrice());
        assertEquals("ACTIVE", listing.getStatus());
        assertEquals(endTime, listing.getEndTime());
    }

    @Test
    void testListingSetters() {
        Listing listing = new Listing();
        listing.setTitle("Laptop Bekas");
        listing.setStatus("DRAFT");

        assertEquals("Laptop Bekas", listing.getTitle());
        assertEquals("DRAFT", listing.getStatus());
    }
}