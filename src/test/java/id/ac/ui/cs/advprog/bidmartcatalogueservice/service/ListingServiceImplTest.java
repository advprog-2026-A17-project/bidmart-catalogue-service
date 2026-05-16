package id.ac.ui.cs.advprog.bidmartcatalogueservice.service;

import id.ac.ui.cs.advprog.bidmartcatalogueservice.model.Category;
import id.ac.ui.cs.advprog.bidmartcatalogueservice.model.Listing;
import id.ac.ui.cs.advprog.bidmartcatalogueservice.model.ListingStatus;
import id.ac.ui.cs.advprog.bidmartcatalogueservice.repository.CategoryRepository;
import id.ac.ui.cs.advprog.bidmartcatalogueservice.repository.ListingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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

    @Mock
    private CategoryRepository categoryRepository;

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
                .status(ListingStatus.ACTIVE)
                .build();
    }

    @Test
    void testCreateListing() {
        Listing listing = Listing.builder()
                .title("Laptop Test")
                .category("Elektronik")
                .startingPrice(new BigDecimal("10000"))
                .status(ListingStatus.ACTIVE)
                .build();

        when(listingRepository.save(any(Listing.class))).thenReturn(listing);

        Listing created = listingService.createListing(listing);

        assertNotNull(created);
        assertEquals("Laptop Test", created.getTitle());
        assertEquals(ListingStatus.ACTIVE, created.getStatus());
        verify(listingRepository, times(1)).save(any(Listing.class));
    }

    @Test
    void testCreateListing_WithNullStatus_DefaultsToDraft() {
        Listing listingWithNullStatus = Listing.builder()
                .id("124")
                .title("Phone Test")
                .startingPrice(new BigDecimal("5000"))
                .build();

        when(listingRepository.save(any(Listing.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Listing created = listingService.createListing(listingWithNullStatus);

        assertNotNull(created);
        assertEquals(ListingStatus.DRAFT, created.getStatus());
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
    void testSearchListings_WithPagination() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Listing> page = new PageImpl<>(Arrays.asList(sampleListing), pageable, 1);
        when(listingRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);

        Page<Listing> result = listingService.searchListings("Elektronik", "Laptop", null, null, ListingStatus.ACTIVE, pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals(1, result.getContent().size());
        verify(listingRepository, times(1)).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    void testUpdateListing_Found() {
        when(listingRepository.findById("123")).thenReturn(Optional.of(sampleListing));
        when(listingRepository.save(any(Listing.class))).thenReturn(sampleListing);

        Listing updatedData = Listing.builder()
                .title("Laptop Test Updated")
                .description("Desc")
                .imageUrl("http://img.com/photo.jpg")
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
    void testUpdateListing_WhenStatusIsSold_ThrowsException() {
        sampleListing.setStatus(ListingStatus.SOLD);
        when(listingRepository.findById("123")).thenReturn(Optional.of(sampleListing));

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> listingService.updateListing("123", new Listing())
        );

        assertTrue(exception.getMessage().contains("Cannot update listing with status"));
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

        assertEquals(ListingStatus.CANCELLED, cancelled.getStatus());
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
    void testCancelListing_WhenStatusIsSold_ThrowsException() {
        sampleListing.setStatus(ListingStatus.SOLD);
        sampleListing.setHasBids(false);
        when(listingRepository.findById("123")).thenReturn(Optional.of(sampleListing));

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> listingService.cancelListing("123")
        );

        assertTrue(exception.getMessage().contains("Cannot cancel listing with status"));
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

    @Test
    void testHandleBidPlaced_WhenStatusIsDraft_ThrowsException() {
        sampleListing.setStatus(ListingStatus.DRAFT);
        when(listingRepository.findById("123")).thenReturn(Optional.of(sampleListing));

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> listingService.handleBidPlaced("123", new BigDecimal("12000"))
        );

        assertTrue(exception.getMessage().contains("Cannot place bid on listing with status"));
        verify(listingRepository, never()).save(any(Listing.class));
    }

    @Test
    void testHandleBidPlaced_WhenStatusIsCancelled_ThrowsException() {
        sampleListing.setStatus(ListingStatus.CANCELLED);
        when(listingRepository.findById("123")).thenReturn(Optional.of(sampleListing));

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> listingService.handleBidPlaced("123", new BigDecimal("12000"))
        );

        assertTrue(exception.getMessage().contains("Cannot place bid on listing with status"));
        verify(listingRepository, never()).save(any(Listing.class));
    }

    @Test
    void testHandleBidPlaced_WhenStatusIsAuctionCreated_Success() {
        sampleListing.setStatus(ListingStatus.AUCTION_CREATED);
        when(listingRepository.findById("123")).thenReturn(Optional.of(sampleListing));
        when(listingRepository.save(any(Listing.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Listing result = listingService.handleBidPlaced("123", new BigDecimal("15000"));

        assertNotNull(result);
        assertTrue(result.isHasBids());
        assertEquals(new BigDecimal("15000"), result.getCurrentPrice());
    }

    @Test
    void testCreateListing_WithCategoryId() {
        Category category = Category.builder().id(1L).name("Elektronik").build();
        Listing listing = Listing.builder()
                .title("Laptop")
                .categoryEntity(Category.builder().id(1L).build())
                .startingPrice(new BigDecimal("10000"))
                .build();

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(listingRepository.save(any(Listing.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Listing created = listingService.createListing(listing);

        assertNotNull(created);
        assertEquals("Elektronik", created.getCategory());
        assertEquals("Elektronik", created.getCategoryEntity().getName());
        verify(categoryRepository).findById(1L);
    }

    @Test
    void testCreateListing_WithInvalidCategoryId() {
        Listing listing = Listing.builder()
                .title("Laptop")
                .categoryEntity(Category.builder().id(999L).build())
                .startingPrice(new BigDecimal("10000"))
                .build();

        when(categoryRepository.findById(999L)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> listingService.createListing(listing)
        );

        assertTrue(exception.getMessage().contains("Category not found"));
        verify(listingRepository, never()).save(any(Listing.class));
    }

    @Test
    void testUpdateListing_WithCategoryId() {
        Category category = Category.builder().id(2L).name("Olahraga").build();
        when(listingRepository.findById("123")).thenReturn(Optional.of(sampleListing));
        when(categoryRepository.findById(2L)).thenReturn(Optional.of(category));
        when(listingRepository.save(any(Listing.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Listing updatedData = Listing.builder()
                .title("Sepeda Lipat")
                .categoryEntity(Category.builder().id(2L).build())
                .sellerId("usr")
                .build();

        Listing updated = listingService.updateListing("123", updatedData);

        assertNotNull(updated);
        assertEquals("Olahraga", sampleListing.getCategory());
        verify(categoryRepository).findById(2L);
    }

    @Test
    void testCreateListing_WithValidImageUrl() {
        Listing listing = Listing.builder()
                .title("Camera")
                .imageUrl("https://example.com/camera.jpg")
                .startingPrice(new BigDecimal("5000"))
                .build();

        when(listingRepository.save(any(Listing.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Listing created = listingService.createListing(listing);

        assertNotNull(created);
        assertEquals("https://example.com/camera.jpg", created.getImageUrl());
    }

    @Test
    void testCreateListing_WithInvalidImageUrl() {
        Listing listing = Listing.builder()
                .title("Camera")
                .imageUrl("not-a-valid-url")
                .startingPrice(new BigDecimal("5000"))
                .build();

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> listingService.createListing(listing)
        );

        assertTrue(exception.getMessage().contains("Invalid image URL"));
        verify(listingRepository, never()).save(any(Listing.class));
    }

    @Test
    void testUpdateListing_WithInvalidImageUrl() {
        when(listingRepository.findById("123")).thenReturn(Optional.of(sampleListing));

        Listing updatedData = Listing.builder()
                .title("Laptop Updated")
                .imageUrl("ftp://invalid.com/image.jpg")
                .sellerId("usr")
                .build();

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> listingService.updateListing("123", updatedData)
        );

        assertTrue(exception.getMessage().contains("Invalid image URL"));
        verify(listingRepository, never()).save(any(Listing.class));
    }


    @Test
    void testPublishListing_FromDraft_Success() {
        sampleListing.setStatus(ListingStatus.DRAFT);
        when(listingRepository.findById("123")).thenReturn(Optional.of(sampleListing));
        when(listingRepository.save(any(Listing.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Listing result = listingService.publishListing("123");

        assertNotNull(result);
        assertEquals(ListingStatus.ACTIVE, result.getStatus());
        verify(listingRepository).save(sampleListing);
    }

    @Test
    void testPublishListing_FromActive_ThrowsException() {
        sampleListing.setStatus(ListingStatus.ACTIVE);
        when(listingRepository.findById("123")).thenReturn(Optional.of(sampleListing));

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> listingService.publishListing("123")
        );

        assertTrue(exception.getMessage().contains("Only DRAFT listings can be published"));
        verify(listingRepository, never()).save(any(Listing.class));
    }

    @Test
    void testPublishListing_NotFound() {
        when(listingRepository.findById("999")).thenReturn(Optional.empty());

        Listing result = listingService.publishListing("999");

        assertNull(result);
        verify(listingRepository, never()).save(any(Listing.class));
    }

    @Test
    void testMarkAuctionCreated_FromActive_Success() {
        sampleListing.setStatus(ListingStatus.ACTIVE);
        when(listingRepository.findById("123")).thenReturn(Optional.of(sampleListing));
        when(listingRepository.save(any(Listing.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Listing result = listingService.markAuctionCreated("123");

        assertNotNull(result);
        assertEquals(ListingStatus.AUCTION_CREATED, result.getStatus());
        verify(listingRepository).save(sampleListing);
    }

    @Test
    void testMarkAuctionCreated_FromDraft_ThrowsException() {
        sampleListing.setStatus(ListingStatus.DRAFT);
        when(listingRepository.findById("123")).thenReturn(Optional.of(sampleListing));

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> listingService.markAuctionCreated("123")
        );

        assertTrue(exception.getMessage().contains("Only ACTIVE listings can be marked as AUCTION_CREATED"));
        verify(listingRepository, never()).save(any(Listing.class));
    }

    @Test
    void testMarkAuctionCreated_NotFound() {
        when(listingRepository.findById("999")).thenReturn(Optional.empty());

        Listing result = listingService.markAuctionCreated("999");

        assertNull(result);
        verify(listingRepository, never()).save(any(Listing.class));
    }

    @Test
    void testMarkSold_FromAuctionCreated_Success() {
        sampleListing.setStatus(ListingStatus.AUCTION_CREATED);
        when(listingRepository.findById("123")).thenReturn(Optional.of(sampleListing));
        when(listingRepository.save(any(Listing.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Listing result = listingService.markSold("123", new BigDecimal("25000"));

        assertNotNull(result);
        assertEquals(ListingStatus.SOLD, result.getStatus());
        assertEquals(new BigDecimal("25000"), result.getCurrentPrice());
        verify(listingRepository).save(sampleListing);
    }

    @Test
    void testMarkSold_FromActive_ThrowsException() {
        sampleListing.setStatus(ListingStatus.ACTIVE);
        when(listingRepository.findById("123")).thenReturn(Optional.of(sampleListing));

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> listingService.markSold("123", new BigDecimal("25000"))
        );

        assertTrue(exception.getMessage().contains("Only AUCTION_CREATED listings can be marked as SOLD"));
        verify(listingRepository, never()).save(any(Listing.class));
    }

    @Test
    void testMarkSold_NotFound() {
        when(listingRepository.findById("999")).thenReturn(Optional.empty());

        Listing result = listingService.markSold("999", new BigDecimal("25000"));

        assertNull(result);
        verify(listingRepository, never()).save(any(Listing.class));
    }

    @Test
    void testMarkUnsold_FromAuctionCreated_Success() {
        sampleListing.setStatus(ListingStatus.AUCTION_CREATED);
        when(listingRepository.findById("123")).thenReturn(Optional.of(sampleListing));
        when(listingRepository.save(any(Listing.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Listing result = listingService.markUnsold("123");

        assertNotNull(result);
        assertEquals(ListingStatus.UNSOLD, result.getStatus());
        verify(listingRepository).save(sampleListing);
    }

    @Test
    void testMarkUnsold_FromActive_ThrowsException() {
        sampleListing.setStatus(ListingStatus.ACTIVE);
        when(listingRepository.findById("123")).thenReturn(Optional.of(sampleListing));

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> listingService.markUnsold("123")
        );

        assertTrue(exception.getMessage().contains("Only AUCTION_CREATED listings can be marked as UNSOLD"));
        verify(listingRepository, never()).save(any(Listing.class));
    }

    @Test
    void testMarkUnsold_NotFound() {
        when(listingRepository.findById("999")).thenReturn(Optional.empty());

        Listing result = listingService.markUnsold("999");

        assertNull(result);
        verify(listingRepository, never()).save(any(Listing.class));
    }

    @Test
    void testCancelListing_FromDraft_Success() {
        sampleListing.setStatus(ListingStatus.DRAFT);
        sampleListing.setHasBids(false);
        when(listingRepository.findById("123")).thenReturn(Optional.of(sampleListing));
        when(listingRepository.save(any(Listing.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Listing result = listingService.cancelListing("123");

        assertNotNull(result);
        assertEquals(ListingStatus.CANCELLED, result.getStatus());
        verify(listingRepository).save(sampleListing);
    }

    @Test
    void testUpdateListing_FromDraft_Success() {
        sampleListing.setStatus(ListingStatus.DRAFT);
        sampleListing.setHasBids(false);
        when(listingRepository.findById("123")).thenReturn(Optional.of(sampleListing));
        when(listingRepository.save(any(Listing.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Listing updatedData = Listing.builder()
                .title("Updated Title")
                .description("Updated Desc")
                .imageUrl("http://img.com/photo.jpg")
                .startingPrice(new BigDecimal("15000"))
                .sellerId("usr")
                .build();

        Listing result = listingService.updateListing("123", updatedData);

        assertNotNull(result);
        assertEquals("Updated Title", result.getTitle());
        verify(listingRepository).save(sampleListing);
    }
}
