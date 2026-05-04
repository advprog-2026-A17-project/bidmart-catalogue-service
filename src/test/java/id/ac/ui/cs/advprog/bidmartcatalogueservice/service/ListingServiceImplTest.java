package id.ac.ui.cs.advprog.bidmartcatalogueservice.service;

import id.ac.ui.cs.advprog.bidmartcatalogueservice.model.Listing;
import id.ac.ui.cs.advprog.bidmartcatalogueservice.repository.ListingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ListingServiceImplTest {

    @Mock
    private ListingRepository listingRepository;

    @InjectMocks
    private ListingServiceImpl listingService;

    private Listing sampleListing;

    @BeforeEach
    void setUp() {
        sampleListing = Listing.builder()
                .id("123")
                .title("Laptop Test")
                .category("Elektronik")
                .startingPrice(new BigDecimal("10000"))
                .status("ACTIVE")
                .build();
    }

    @Test
    void testCreateListing() {
        when(listingRepository.save(any(Listing.class))).thenReturn(sampleListing);

        Listing created = listingService.createListing(sampleListing);

        assertNotNull(created);
        assertEquals("Laptop Test", created.getTitle());
        assertEquals("ACTIVE", created.getStatus());
        verify(listingRepository, times(1)).save(any(Listing.class));
    }

    @Test
    void testCreateListing_WithNullStatus() {
        Listing listingWithNullStatus = Listing.builder()
                .id("124")
                .title("Phone Test")
                .startingPrice(new BigDecimal("5000"))
                .build();

        when(listingRepository.save(any(Listing.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Listing created = listingService.createListing(listingWithNullStatus);

        assertNotNull(created);
        assertEquals("ACTIVE", created.getStatus());
        verify(listingRepository, times(1)).save(listingWithNullStatus);
    }

    @Test
    void testGetAllListings() {
        when(listingRepository.findAll()).thenReturn(Arrays.asList(sampleListing));

        List<Listing> listings = listingService.getAllListings();

        assertFalse(listings.isEmpty());
        assertEquals(1, listings.size());
        verify(listingRepository, times(1)).findAll();
    }

    @Test
    void testGetListingById_Found() {
        when(listingRepository.findById("123")).thenReturn(Optional.of(sampleListing));

        Listing found = listingService.getListingById("123");

        assertNotNull(found);
        assertEquals("123", found.getId());
    }

    @Test
    void testGetListingById_NotFound() {
        when(listingRepository.findById("999")).thenReturn(Optional.empty());

        Listing found = listingService.getListingById("999");

        assertNull(found);
    }

    @Test
    @SuppressWarnings("unchecked")
    void testSearchListings() {
        when(listingRepository.findAll(any(Specification.class))).thenReturn(Arrays.asList(sampleListing));

        List<Listing> result = listingService.searchListings("Elektronik", "Laptop", null, null, "ACTIVE");

        assertEquals(1, result.size());
        verify(listingRepository, times(1)).findAll(any(Specification.class));
    }

    @Test
    void testUpdateListing_Found() {
        when(listingRepository.findById("123")).thenReturn(Optional.of(sampleListing));
        when(listingRepository.save(any(Listing.class))).thenReturn(sampleListing);

        Listing updatedData = Listing.builder()
                .title("Laptop Test Updated")
                .description("Desc")
                .imageUrl("http://img.com")
                .startingPrice(new BigDecimal("12000"))
                .currentPrice(new BigDecimal("15000"))
                .sellerId("usr")
                .build();

        Listing updated = listingService.updateListing("123", updatedData);

        assertNotNull(updated);
        verify(listingRepository, times(1)).save(sampleListing);
        assertEquals("Laptop Test Updated", sampleListing.getTitle());
    }

    @Test
    void testUpdateListing_NotFound() {
        when(listingRepository.findById("999")).thenReturn(Optional.empty());

        Listing updatedData = new Listing();
        Listing updated = listingService.updateListing("999", updatedData);

        assertNull(updated);
        verify(listingRepository, never()).save(any(Listing.class));
    }

    @Test
    void testDeleteListing() {
        doNothing().when(listingRepository).deleteById("123");

        listingService.deleteListing("123");

        verify(listingRepository, times(1)).deleteById("123");
    }

    @Test
    void testCancelListing_WhenNoBids() {
        sampleListing.setHasBids(false);
        when(listingRepository.findById("123")).thenReturn(Optional.of(sampleListing));
        when(listingRepository.save(any(Listing.class))).thenReturn(sampleListing);

        Listing cancelled = listingService.cancelListing("123");

        assertEquals("CANCELLED", cancelled.getStatus());
        verify(listingRepository).save(sampleListing);
    }

    @Test
    void testCancelListing_WhenHasBids() {
        sampleListing.setHasBids(true);
        when(listingRepository.findById("123")).thenReturn(Optional.of(sampleListing));

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> listingService.cancelListing("123")
        );

        assertEquals("Listing has active bids", exception.getMessage());
        verify(listingRepository, never()).save(any(Listing.class));
    }

    @Test
    void testUpdateListing_WhenHasBids_ThrowsException() {
        sampleListing.setHasBids(true);
        when(listingRepository.findById("123")).thenReturn(Optional.of(sampleListing));

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> listingService.updateListing("123", new Listing())
        );

        assertEquals("Cannot update listing with active bids", exception.getMessage());
        verify(listingRepository, never()).save(any(Listing.class));
    }

    @Test
    void testHandleBidPlaced_Success() {
        when(listingRepository.findById("123")).thenReturn(Optional.of(sampleListing));
        when(listingRepository.save(any(Listing.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Listing result = listingService.handleBidPlaced("123", new BigDecimal("12000"));

        assertNotNull(result);
        assertTrue(result.isHasBids());
        assertEquals(new BigDecimal("12000"), result.getCurrentPrice());
        verify(listingRepository).save(sampleListing);
    }

    @Test
    void testHandleBidPlaced_NotFound() {
        when(listingRepository.findById("999")).thenReturn(Optional.empty());

        Listing result = listingService.handleBidPlaced("999", new BigDecimal("12000"));

        assertNull(result);
        verify(listingRepository, never()).save(any(Listing.class));
    }
}
