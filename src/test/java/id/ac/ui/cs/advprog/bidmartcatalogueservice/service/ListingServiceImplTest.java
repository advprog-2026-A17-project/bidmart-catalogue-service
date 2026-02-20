package id.ac.ui.cs.advprog.bidmartcatalogueservice.service;

import id.ac.ui.cs.advprog.bidmartcatalogueservice.model.Listing;
import id.ac.ui.cs.advprog.bidmartcatalogueservice.repository.ListingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
}