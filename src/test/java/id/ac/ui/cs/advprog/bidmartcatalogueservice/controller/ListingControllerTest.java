package id.ac.ui.cs.advprog.bidmartcatalogueservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import id.ac.ui.cs.advprog.bidmartcatalogueservice.config.AuthInterceptor;
import id.ac.ui.cs.advprog.bidmartcatalogueservice.dto.BidPlacedEvent;
import id.ac.ui.cs.advprog.bidmartcatalogueservice.event.ListingEventPublisher;
import id.ac.ui.cs.advprog.bidmartcatalogueservice.metrics.BidmartCatalogueMetrics;
import id.ac.ui.cs.advprog.bidmartcatalogueservice.model.Listing;
import id.ac.ui.cs.advprog.bidmartcatalogueservice.model.ListingStatus;
import id.ac.ui.cs.advprog.bidmartcatalogueservice.service.CatalogAccessPolicy;
import id.ac.ui.cs.advprog.bidmartcatalogueservice.service.ListingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ListingController.class)
class ListingControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @MockitoBean
        private ListingService listingService;

        @MockitoBean
        private AuthInterceptor authInterceptor;

        @MockitoBean
        private ListingEventPublisher listingEventPublisher;

        @MockitoBean
        private BidmartCatalogueMetrics catalogueMetrics;

        @MockitoBean
        private CatalogAccessPolicy catalogAccessPolicy;

        @Autowired
        private ObjectMapper objectMapper;

        private Listing sampleListing;

        @BeforeEach
        void setUp() throws Exception {
                sampleListing = Listing.builder()
                                .id("123")
                                .sellerId("seller-123")
                                .title("Kamera Test")
                                .category("Fotografi")
                                .startingPrice(new BigDecimal("500000"))
                                .status(ListingStatus.ACTIVE)
                                .build();
                when(authInterceptor.preHandle(any(), any(), any())).thenReturn(true);
        }

        @Test
        void testGetAllListingsEndpoint() throws Exception {
                when(listingService.getAllListings()).thenReturn(Arrays.asList(sampleListing));

                mockMvc.perform(get("/api/v1/catalogue/listings"))
                                .andExpect(status().isOk())
                                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                                .andExpect(jsonPath("$[0].id").value("123"))
                                .andExpect(jsonPath("$[0].title").value("Kamera Test"));
        }

        @Test
        void testGetListingByIdEndpoint() throws Exception {
                when(listingService.getListingById("123")).thenReturn(sampleListing);

                mockMvc.perform(get("/api/v1/catalogue/listings/123"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.title").value("Kamera Test"));
        }

        @Test
        void testGetListingSummaryByIdEndpoint() throws Exception {
                when(listingService.getListingById("123")).thenReturn(sampleListing);

                mockMvc.perform(get("/api/v1/catalogue/listings/123/summary"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.id").value("123"))
                                .andExpect(jsonPath("$.sellerId").value("seller-123"))
                                .andExpect(jsonPath("$.status").value("ACTIVE"));
        }

        @Test
        void testGetListingSummaryByIdEndpoint_NotFound() throws Exception {
                when(listingService.getListingById("999")).thenReturn(null);

                mockMvc.perform(get("/api/v1/catalogue/listings/999/summary"))
                                .andExpect(status().isNotFound());
        }

        @Test
        void testCreateListingEndpoint() throws Exception {
                when(listingService.createListing(any(Listing.class))).thenReturn(sampleListing);

                String requestBody = objectMapper.writeValueAsString(sampleListing);

                mockMvc.perform(post("/api/v1/catalogue/listings")
                                .contentType(MediaType.APPLICATION_JSON)
                                .header("X-User-Id", "seller-123")
                                .content(requestBody))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.title").value("Kamera Test"));

                verify(listingEventPublisher).publishListingCreated(any());
        }

        @Test
        void testCreateListing_WithInvalidImageUrl_Returns400() throws Exception {
                when(listingService.createListing(any(Listing.class)))
                                .thenThrow(new IllegalArgumentException("Invalid image URL format: not-a-url"));

                Listing badListing = Listing.builder()
                                .title("Kamera Test")
                                .imageUrl("not-a-url")
                                .startingPrice(new BigDecimal("500000"))
                                .build();
                String requestBody = objectMapper.writeValueAsString(badListing);

                mockMvc.perform(post("/api/v1/catalogue/listings")
                                .contentType(MediaType.APPLICATION_JSON)
                                .header("X-User-Id", "seller-123")
                                .content(requestBody))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.message").value("Invalid image URL format: not-a-url"));
        }

        @Test
        void testUpdateListingEndpoint_Found() throws Exception {
                Listing updatedListing = Listing.builder().id("123").title("Kamera Update").build();
                when(listingService.getListingById("123")).thenReturn(sampleListing);
                when(listingService.updateListing(eq("123"), any(Listing.class))).thenReturn(updatedListing);

                String requestBody = objectMapper.writeValueAsString(updatedListing);

                mockMvc.perform(put("/api/v1/catalogue/listings/123")
                                .contentType(MediaType.APPLICATION_JSON)
                                .header("X-User-Id", "seller-123")
                                .content(requestBody))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.title").value("Kamera Update"));
        }

        @Test
        void testUpdateListingEndpoint_NotFound() throws Exception {
                when(listingService.getListingById("999")).thenReturn(null);

                Listing updatedListing = Listing.builder().id("999").title("NotFound").build();
                String requestBody = objectMapper.writeValueAsString(updatedListing);

                mockMvc.perform(put("/api/v1/catalogue/listings/999")
                                .contentType(MediaType.APPLICATION_JSON)
                                .header("X-User-Id", "seller-123")
                                .content(requestBody))
                                .andExpect(status().isNotFound());
        }

        @Test
        void testUpdateListingEndpoint_Forbidden() throws Exception {
                when(listingService.getListingById("123")).thenReturn(sampleListing);

                Listing updatedListing = Listing.builder().id("123").title("Kamera Update").build();
                String requestBody = objectMapper.writeValueAsString(updatedListing);

                mockMvc.perform(put("/api/v1/catalogue/listings/123")
                                .contentType(MediaType.APPLICATION_JSON)
                                .header("X-User-Id", "wrong-seller")
                                .content(requestBody))
                                .andExpect(status().isForbidden());
        }

        @Test
        void testUpdateListingEndpoint_Conflict_WhenHasBids() throws Exception {
                when(listingService.getListingById("123")).thenReturn(sampleListing);
                when(listingService.updateListing(eq("123"), any(Listing.class)))
                                .thenThrow(new IllegalStateException("Cannot update listing with active bids"));

                Listing updatedListing = Listing.builder().id("123").title("Kamera Update").build();
                String requestBody = objectMapper.writeValueAsString(updatedListing);

                mockMvc.perform(put("/api/v1/catalogue/listings/123")
                                .contentType(MediaType.APPLICATION_JSON)
                                .header("X-User-Id", "seller-123")
                                .content(requestBody))
                                .andExpect(status().isConflict())
                                .andExpect(jsonPath("$.message").value("Cannot update listing with active bids"));
        }

        @Test
        void testUpdateListing_WithInvalidImageUrl_Returns400() throws Exception {
                when(listingService.getListingById("123")).thenReturn(sampleListing);
                when(listingService.updateListing(eq("123"), any(Listing.class)))
                                .thenThrow(new IllegalArgumentException("Invalid image URL format: ftp://bad.com"));

                Listing badListing = Listing.builder()
                                .id("123")
                                .title("Kamera Update")
                                .imageUrl("ftp://bad.com")
                                .build();
                String requestBody = objectMapper.writeValueAsString(badListing);

                mockMvc.perform(put("/api/v1/catalogue/listings/123")
                                .contentType(MediaType.APPLICATION_JSON)
                                .header("X-User-Id", "seller-123")
                                .content(requestBody))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.message").value("Invalid image URL format: ftp://bad.com"));
        }

        @Test
        void testDeleteListingEndpoint_Found() throws Exception {
                when(listingService.getListingById("123")).thenReturn(sampleListing);
                doNothing().when(listingService).deleteListing("123");

                mockMvc.perform(delete("/api/v1/catalogue/listings/123")
                                .header("X-User-Id", "seller-123"))
                                .andExpect(status().isNoContent());
        }

        @Test
        void testDeleteListingEndpoint_NotFound() throws Exception {
                when(listingService.getListingById("999")).thenReturn(null);

                mockMvc.perform(delete("/api/v1/catalogue/listings/999")
                                .header("X-User-Id", "seller-123"))
                                .andExpect(status().isNotFound());
        }

        @Test
        void testDeleteListingEndpoint_Forbidden() throws Exception {
                when(listingService.getListingById("123")).thenReturn(sampleListing);

                mockMvc.perform(delete("/api/v1/catalogue/listings/123")
                                .header("X-User-Id", "wrong-seller"))
                                .andExpect(status().isForbidden());
        }

        @Test
        @SuppressWarnings("unchecked")
        void testSearchListingsEndpoint_WithPagination() throws Exception {
                Page<Listing> page = new PageImpl<>(Arrays.asList(sampleListing));
                when(listingService.searchListings(eq("Fotografi"), isNull(), eq("Kamera"), any(), any(),
                                eq(ListingStatus.ACTIVE), any(Pageable.class)))
                                .thenReturn(page);

                mockMvc.perform(get("/api/v1/catalogue/listings/search")
                                .param("category", "Fotografi")
                                .param("keyword", "Kamera")
                                .param("status", "ACTIVE")
                                .param("page", "0")
                                .param("size", "10")
                                .param("sortBy", "title")
                                .param("sortDir", "asc"))
                                .andExpect(status().isOk())
                                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                                .andExpect(jsonPath("$.content[0].title").value("Kamera Test"))
                                .andExpect(jsonPath("$.totalElements").value(1));
        }

        @Test
        @SuppressWarnings("unchecked")
        void testSearchListingsEndpoint_WithDefaults() throws Exception {
                Page<Listing> page = new PageImpl<>(Arrays.asList(sampleListing));
                when(listingService.searchListings(any(), any(), any(), any(), any(), any(), any(Pageable.class)))
                                .thenReturn(page);

                mockMvc.perform(get("/api/v1/catalogue/listings/search"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.content[0].title").value("Kamera Test"));
        }

        @Test
        void testCancelListingEndpoint_WhenNoBids() throws Exception {
                when(listingService.getListingById("123")).thenReturn(sampleListing);
                Listing cancelledListing = Listing.builder()
                                .id("123")
                                .sellerId("seller-123")
                                .title("Kamera Test")
                                .status(ListingStatus.CANCELLED)
                                .hasBids(false)
                                .build();
                when(listingService.cancelListing("123")).thenReturn(cancelledListing);

                mockMvc.perform(post("/api/v1/catalogue/listings/123/cancel")
                                .header("X-User-Id", "seller-123"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.status").value("CANCELLED"));
        }

        @Test
        void testCancelListingEndpoint_WhenListingHasBids() throws Exception {
                when(listingService.getListingById("123")).thenReturn(sampleListing);
                when(listingService.cancelListing("123"))
                                .thenThrow(new IllegalStateException("Listing has active bids"));

                mockMvc.perform(post("/api/v1/catalogue/listings/123/cancel")
                                .header("X-User-Id", "seller-123"))
                                .andExpect(status().isConflict())
                                .andExpect(jsonPath("$.message").value("Listing has active bids"));
        }

        @Test
        void testCancelListingEndpoint_Forbidden() throws Exception {
                when(listingService.getListingById("123")).thenReturn(sampleListing);

                mockMvc.perform(post("/api/v1/catalogue/listings/123/cancel")
                                .header("X-User-Id", "wrong-seller"))
                                .andExpect(status().isForbidden());
        }

        @Test
        void testCancelListingEndpoint_NotFound() throws Exception {
                when(listingService.getListingById("999")).thenReturn(null);

                mockMvc.perform(post("/api/v1/catalogue/listings/999/cancel")
                                .header("X-User-Id", "seller-123"))
                                .andExpect(status().isNotFound());
        }

        @Test
        void testBidPlacedEndpoint_Success() throws Exception {
                Listing updatedListing = Listing.builder()
                                .id("123")
                                .sellerId("seller-123")
                                .title("Kamera Test")
                                .currentPrice(new BigDecimal("600000"))
                                .hasBids(true)
                                .build();
                when(listingService.handleBidPlaced(eq("123"), any(BigDecimal.class))).thenReturn(updatedListing);

                BidPlacedEvent event = new BidPlacedEvent("123", new BigDecimal("600000"));
                String requestBody = objectMapper.writeValueAsString(event);

                mockMvc.perform(post("/api/v1/catalogue/listings/123/bid")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.hasBids").value(true))
                                .andExpect(jsonPath("$.currentPrice").value(600000));
        }

        @Test
        void testBidPlacedEndpoint_NotFound() throws Exception {
                when(listingService.handleBidPlaced(eq("999"), any(BigDecimal.class))).thenReturn(null);

                BidPlacedEvent event = new BidPlacedEvent("999", new BigDecimal("600000"));
                String requestBody = objectMapper.writeValueAsString(event);

                mockMvc.perform(post("/api/v1/catalogue/listings/999/bid")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody))
                                .andExpect(status().isNotFound());
        }

        @Test
        void testBidPlacedEndpoint_Conflict_WhenInvalidStatus() throws Exception {
                when(listingService.handleBidPlaced(eq("123"), any(BigDecimal.class)))
                                .thenThrow(new IllegalStateException("Cannot place bid on listing with status: DRAFT"));

                BidPlacedEvent event = new BidPlacedEvent("123", new BigDecimal("600000"));
                String requestBody = objectMapper.writeValueAsString(event);

                mockMvc.perform(post("/api/v1/catalogue/listings/123/bid")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody))
                                .andExpect(status().isConflict())
                                .andExpect(jsonPath("$.message")
                                                .value("Cannot place bid on listing with status: DRAFT"));
        }

        @Test
        void testPublishEndpoint_Success() throws Exception {
                Listing publishedListing = Listing.builder()
                                .id("123")
                                .sellerId("seller-123")
                                .title("Kamera Test")
                                .status(ListingStatus.ACTIVE)
                                .build();
                when(listingService.getListingById("123")).thenReturn(sampleListing);
                when(listingService.publishListing("123")).thenReturn(publishedListing);

                mockMvc.perform(post("/api/v1/catalogue/listings/123/publish")
                                .header("X-User-Id", "seller-123"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.status").value("ACTIVE"));
        }

        @Test
        void testPublishEndpoint_NotFound() throws Exception {
                when(listingService.getListingById("999")).thenReturn(null);

                mockMvc.perform(post("/api/v1/catalogue/listings/999/publish")
                                .header("X-User-Id", "seller-123"))
                                .andExpect(status().isNotFound());
        }

        @Test
        void testPublishEndpoint_Forbidden() throws Exception {
                when(listingService.getListingById("123")).thenReturn(sampleListing);

                mockMvc.perform(post("/api/v1/catalogue/listings/123/publish")
                                .header("X-User-Id", "wrong-seller"))
                                .andExpect(status().isForbidden());
        }

        @Test
        void testPublishEndpoint_Conflict() throws Exception {
                when(listingService.getListingById("123")).thenReturn(sampleListing);
                when(listingService.publishListing("123"))
                                .thenThrow(new IllegalStateException("Only DRAFT listings can be published"));

                mockMvc.perform(post("/api/v1/catalogue/listings/123/publish")
                                .header("X-User-Id", "seller-123"))
                                .andExpect(status().isConflict())
                                .andExpect(jsonPath("$.message").value("Only DRAFT listings can be published"));
        }

        @Test
        void testExtendEndpoint_Success() throws Exception {
                Listing result = Listing.builder()
                                .id("123")
                                .sellerId("seller-123")
                                .status(ListingStatus.EXTENDED)
                                .build();
                when(listingService.getListingById("123")).thenReturn(sampleListing);
                when(listingService.markExtended("123")).thenReturn(result);

                mockMvc.perform(post("/api/v1/catalogue/listings/123/extend")
                                .header("X-User-Id", "seller-123"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.status").value("EXTENDED"));
        }

        @Test
        void testExtendEndpoint_NotFound() throws Exception {
                when(listingService.getListingById("999")).thenReturn(null);

                mockMvc.perform(post("/api/v1/catalogue/listings/999/extend")
                                .header("X-User-Id", "seller-123"))
                                .andExpect(status().isNotFound());
        }

        @Test
        void testExtendEndpoint_Forbidden() throws Exception {
                when(listingService.getListingById("123")).thenReturn(sampleListing);

                mockMvc.perform(post("/api/v1/catalogue/listings/123/extend")
                                .header("X-User-Id", "wrong-seller"))
                                .andExpect(status().isForbidden());
        }

        @Test
        void testWonEndpoint_Success() throws Exception {
                Listing result = Listing.builder()
                                .id("123")
                                .sellerId("seller-123")
                                .status(ListingStatus.WON)
                                .currentPrice(new BigDecimal("750000"))
                                .build();
                when(listingService.getListingById("123")).thenReturn(sampleListing);
                when(listingService.markWon(eq("123"), any(BigDecimal.class))).thenReturn(result);

                String body = objectMapper.writeValueAsString(Map.of("finalPrice", new BigDecimal("750000")));

                mockMvc.perform(post("/api/v1/catalogue/listings/123/won")
                                .contentType(MediaType.APPLICATION_JSON)
                                .header("X-User-Id", "seller-123")
                                .content(body))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.status").value("WON"))
                                .andExpect(jsonPath("$.currentPrice").value(750000));
        }

        @Test
        void testWonEndpoint_NotFound() throws Exception {
                when(listingService.getListingById("999")).thenReturn(null);

                String body = objectMapper.writeValueAsString(Map.of("finalPrice", new BigDecimal("750000")));

                mockMvc.perform(post("/api/v1/catalogue/listings/999/won")
                                .contentType(MediaType.APPLICATION_JSON)
                                .header("X-User-Id", "seller-123")
                                .content(body))
                                .andExpect(status().isNotFound());
        }

        @Test
        void testWonEndpoint_Forbidden() throws Exception {
                when(listingService.getListingById("123")).thenReturn(sampleListing);

                String body = objectMapper.writeValueAsString(Map.of("finalPrice", new BigDecimal("750000")));

                mockMvc.perform(post("/api/v1/catalogue/listings/123/won")
                                .contentType(MediaType.APPLICATION_JSON)
                                .header("X-User-Id", "wrong-seller")
                                .content(body))
                                .andExpect(status().isForbidden());
        }


        @Test
        void testUnsoldEndpoint_Success() throws Exception {
                Listing result = Listing.builder()
                                .id("123")
                                .sellerId("seller-123")
                                .status(ListingStatus.UNSOLD)
                                .build();
                when(listingService.getListingById("123")).thenReturn(sampleListing);
                when(listingService.markUnsold("123")).thenReturn(result);

                mockMvc.perform(post("/api/v1/catalogue/listings/123/unsold")
                                .header("X-User-Id", "seller-123"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.status").value("UNSOLD"));
        }

        @Test
        void testUnsoldEndpoint_NotFound() throws Exception {
                when(listingService.getListingById("999")).thenReturn(null);

                mockMvc.perform(post("/api/v1/catalogue/listings/999/unsold")
                                .header("X-User-Id", "seller-123"))
                                .andExpect(status().isNotFound());
        }

        @Test
        void testUnsoldEndpoint_Forbidden() throws Exception {
                when(listingService.getListingById("123")).thenReturn(sampleListing);

                mockMvc.perform(post("/api/v1/catalogue/listings/123/unsold")
                                .header("X-User-Id", "wrong-seller"))
                                .andExpect(status().isForbidden());
        }

        @Test
        void testAdminCloseEndpoint() throws Exception {
                Listing cancelled = Listing.builder()
                        .id("123")
                        .sellerId("seller-123")
                        .status(ListingStatus.CANCELLED)
                        .build();
                when(listingService.getListingById("123")).thenReturn(sampleListing);
                when(listingService.adminCloseListing("123")).thenReturn(cancelled);

                mockMvc.perform(post("/api/v1/catalogue/listings/123/admin/close")
                                .header("X-User-Roles", "ADMIN")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"reason\":\"Policy violation\"}"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.status").value("CANCELLED"));

                verify(listingEventPublisher).publishListingClosedByAdmin(any());
        }
}
