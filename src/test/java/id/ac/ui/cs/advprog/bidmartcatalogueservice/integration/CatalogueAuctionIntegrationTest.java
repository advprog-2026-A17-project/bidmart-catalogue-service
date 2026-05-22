package id.ac.ui.cs.advprog.bidmartcatalogueservice.integration;

import id.ac.ui.cs.advprog.bidmartcatalogueservice.model.Listing;
import id.ac.ui.cs.advprog.bidmartcatalogueservice.model.ListingStatus;
import id.ac.ui.cs.advprog.bidmartcatalogueservice.repository.ListingRepository;
import id.ac.ui.cs.advprog.bidmartcatalogueservice.service.ListingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(properties = "grpc.server.port=0")
@ActiveProfiles("test")
class CatalogueAuctionIntegrationTest {

    @Autowired
    private ListingService listingService;

    @Autowired
    private ListingRepository listingRepository;

    @BeforeEach
    void setUp() {
        listingRepository.deleteAll();
    }
    
/**
 * Integration test antara catalogue dan auction lifecycle.
 * 
 * Test ini memvalidasi:
 * 1. Active listing validation — hanya listing ACTIVE yang bisa menerima bid
 * 2. Eventual price update — price update via bid dan mark sold
 * 3. Full lifecycle — DRAFT → ACTIVE/EXTENDED → WON/UNSOLD
 */

    @Test
    void testFullLifecycle_DraftToSold() {
        Listing listing = Listing.builder()
                .sellerId("seller-001")
                .title("Laptop Gaming")
                .description("Laptop gaming kondisi mulus")
                .category("Elektronik")
                .imageUrl("https://example.com/laptop.jpg")
                .startingPrice(new BigDecimal("10000000"))
                .currentPrice(new BigDecimal("10000000"))
                .endTime(LocalDateTime.now().plusDays(7))
                .build();

        Listing created = listingService.createListing(listing);
        assertNotNull(created.getId());
        assertEquals(ListingStatus.DRAFT, created.getStatus());

        Listing published = listingService.publishListing(created.getId());
        assertEquals(ListingStatus.ACTIVE, published.getStatus());

        Listing extended = listingService.markExtended(published.getId());
        assertEquals(ListingStatus.EXTENDED, extended.getStatus());

        Listing bidded = listingService.handleBidPlaced(extended.getId(), new BigDecimal("12000000"));
        assertNotNull(bidded);
        assertTrue(bidded.isHasBids());
        assertEquals(0, new BigDecimal("12000000").compareTo(bidded.getCurrentPrice()));

        Listing sold = listingService.markWon(bidded.getId(), new BigDecimal("15000000"));
        assertEquals(ListingStatus.WON, sold.getStatus());
        assertEquals(0, new BigDecimal("15000000").compareTo(sold.getCurrentPrice()));

        Listing fromDb = listingService.getListingById(sold.getId());
        assertNotNull(fromDb);
        assertEquals(ListingStatus.WON, fromDb.getStatus());
        assertEquals(0, new BigDecimal("15000000").compareTo(fromDb.getCurrentPrice()));
    }


    @Test
    void testFullLifecycle_DraftToUnsold() {
        Listing listing = Listing.builder()
                .sellerId("seller-002")
                .title("Sepeda Lipat")
                .description("Sepeda lipat jarang dipakai")
                .category("Olahraga")
                .imageUrl("https://example.com/sepeda.jpg")
                .startingPrice(new BigDecimal("5000000"))
                .currentPrice(new BigDecimal("5000000"))
                .endTime(LocalDateTime.now().plusDays(3))
                .build();

        Listing created = listingService.createListing(listing);
        assertEquals(ListingStatus.DRAFT, created.getStatus());

        Listing published = listingService.publishListing(created.getId());
        assertEquals(ListingStatus.ACTIVE, published.getStatus());

        Listing unsold = listingService.markUnsold(published.getId());
        assertEquals(ListingStatus.UNSOLD, unsold.getStatus());
    }


    @Test
    void testBidPlaced_OnActiveListing_Success() {
        Listing listing = Listing.builder()
                .sellerId("seller-003")
                .title("Kamera DSLR")
                .category("Fotografi")
                .imageUrl("https://example.com/kamera.jpg")
                .startingPrice(new BigDecimal("8000000"))
                .currentPrice(new BigDecimal("8000000"))
                .status(ListingStatus.ACTIVE)
                .endTime(LocalDateTime.now().plusDays(5))
                .build();

        Listing created = listingService.createListing(listing);
        assertEquals(ListingStatus.ACTIVE, created.getStatus());

        Listing bidded = listingService.handleBidPlaced(created.getId(), new BigDecimal("9000000"));
        assertNotNull(bidded);
        assertTrue(bidded.isHasBids());
        assertEquals(0, new BigDecimal("9000000").compareTo(bidded.getCurrentPrice()));
    }

    @Test
    void testBidPlaced_OnDraftListing_ThrowsException() {
        Listing listing = Listing.builder()
                .sellerId("seller-004")
                .title("Monitor Ultrawide")
                .category("Elektronik")
                .imageUrl("https://example.com/monitor.jpg")
                .startingPrice(new BigDecimal("3000000"))
                .currentPrice(new BigDecimal("3000000"))
                .endTime(LocalDateTime.now().plusDays(5))
                .build();

        Listing created = listingService.createListing(listing);
        assertEquals(ListingStatus.DRAFT, created.getStatus());

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> listingService.handleBidPlaced(created.getId(), new BigDecimal("4000000"))
        );
        assertTrue(exception.getMessage().contains("Cannot place bid on listing with status"));
    }

    @Test
    void testBidPlaced_OnCancelledListing_ThrowsException() {
        Listing listing = Listing.builder()
                .sellerId("seller-005")
                .title("Headphone Wireless")
                .category("Elektronik")
                .imageUrl("https://example.com/headphone.jpg")
                .startingPrice(new BigDecimal("500000"))
                .currentPrice(new BigDecimal("500000"))
                .status(ListingStatus.ACTIVE)
                .endTime(LocalDateTime.now().plusDays(5))
                .build();

        Listing created = listingService.createListing(listing);
        Listing cancelled = listingService.cancelListing(created.getId());
        assertEquals(ListingStatus.CANCELLED, cancelled.getStatus());

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> listingService.handleBidPlaced(created.getId(), new BigDecimal("600000"))
        );
        assertTrue(exception.getMessage().contains("Cannot place bid on listing with status"));
    }


    @Test
    void testEventualPriceUpdate_MultipleBids() {
        Listing listing = Listing.builder()
                .sellerId("seller-006")
                .title("Jam Tangan Vintage")
                .category("Aksesoris")
                .imageUrl("https://example.com/jam.jpg")
                .startingPrice(new BigDecimal("2000000"))
                .currentPrice(new BigDecimal("2000000"))
                .status(ListingStatus.ACTIVE)
                .endTime(LocalDateTime.now().plusDays(5))
                .build();

        Listing created = listingService.createListing(listing);

        Listing bid1 = listingService.handleBidPlaced(created.getId(), new BigDecimal("2500000"));
        assertEquals(0, new BigDecimal("2500000").compareTo(bid1.getCurrentPrice()));

        Listing bid2 = listingService.handleBidPlaced(created.getId(), new BigDecimal("3000000"));
        assertEquals(0, new BigDecimal("3000000").compareTo(bid2.getCurrentPrice()));

        Listing bid3 = listingService.handleBidPlaced(created.getId(), new BigDecimal("3500000"));
        assertEquals(0, new BigDecimal("3500000").compareTo(bid3.getCurrentPrice()));

        Listing fromDb = listingService.getListingById(created.getId());
        assertEquals(0, new BigDecimal("3500000").compareTo(fromDb.getCurrentPrice()));
        assertTrue(fromDb.isHasBids());
    }


    @Test
    void testInvalidTransition_PublishFromActive_ThrowsException() {
        Listing listing = Listing.builder()
                .sellerId("seller-007")
                .title("Tablet Samsung")
                .category("Elektronik")
                .imageUrl("https://example.com/tablet.jpg")
                .startingPrice(new BigDecimal("4000000"))
                .currentPrice(new BigDecimal("4000000"))
                .status(ListingStatus.ACTIVE)
                .endTime(LocalDateTime.now().plusDays(5))
                .build();

        Listing created = listingService.createListing(listing);

        assertThrows(IllegalStateException.class, () -> listingService.publishListing(created.getId()));
    }

    @Test
    void testInvalidTransition_MarkWonFromCancelled_ThrowsException() {
        Listing listing = Listing.builder()
                .sellerId("seller-008")
                .title("Speaker Bluetooth")
                .category("Elektronik")
                .imageUrl("https://example.com/speaker.jpg")
                .startingPrice(new BigDecimal("1000000"))
                .currentPrice(new BigDecimal("1000000"))
                .status(ListingStatus.ACTIVE)
                .endTime(LocalDateTime.now().plusDays(5))
                .build();

        Listing created = listingService.createListing(listing);

        Listing cancelled = listingService.cancelListing(created.getId());
        assertEquals(ListingStatus.CANCELLED, cancelled.getStatus());
        assertThrows(IllegalStateException.class, () -> listingService.markWon(created.getId(), new BigDecimal("1500000")));
    }

    @Test
    void testInvalidTransition_CancelFromSold_ThrowsException() {
        Listing listing = Listing.builder()
                .sellerId("seller-009")
                .title("Buku Antik")
                .category("Koleksi")
                .imageUrl("https://example.com/buku.jpg")
                .startingPrice(new BigDecimal("500000"))
                .currentPrice(new BigDecimal("500000"))
                .status(ListingStatus.ACTIVE)
                .endTime(LocalDateTime.now().plusDays(5))
                .build();

        Listing created = listingService.createListing(listing);
        listingService.markWon(created.getId(), new BigDecimal("750000"));

        assertThrows(
                IllegalStateException.class,
                () -> listingService.cancelListing(created.getId())
        );
    }

    @Test
    void testCancelFromDraft_Success() {
        Listing listing = Listing.builder()
                .sellerId("seller-010")
                .title("Perhiasan Emas")
                .category("Aksesoris")
                .imageUrl("https://example.com/emas.jpg")
                .startingPrice(new BigDecimal("10000000"))
                .currentPrice(new BigDecimal("10000000"))
                .endTime(LocalDateTime.now().plusDays(7))
                .build();

        Listing created = listingService.createListing(listing);
        assertEquals(ListingStatus.DRAFT, created.getStatus());

        Listing cancelled = listingService.cancelListing(created.getId());
        assertEquals(ListingStatus.CANCELLED, cancelled.getStatus());
    }
}
