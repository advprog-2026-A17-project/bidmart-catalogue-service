package id.ac.ui.cs.advprog.bidmartcatalogueservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import id.ac.ui.cs.advprog.bidmartcatalogueservice.model.Listing;
import id.ac.ui.cs.advprog.bidmartcatalogueservice.service.ListingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Arrays;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ListingController.class)
class ListingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ListingService listingService;

    @Autowired
    private ObjectMapper objectMapper;

    private Listing sampleListing;

    @BeforeEach
    void setUp() {
        sampleListing = Listing.builder()
                .id("123")
                .title("Kamera Test")
                .category("Fotografi")
                .startingPrice(new BigDecimal("500000"))
                .build();
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
    void testCreateListingEndpoint() throws Exception {
        when(listingService.createListing(any(Listing.class))).thenReturn(sampleListing);

        String requestBody = objectMapper.writeValueAsString(sampleListing);

        mockMvc.perform(post("/api/v1/catalogue/listings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Kamera Test"));
    }

    @Test
    void testUpdateListingEndpoint_Found() throws Exception {
        Listing updatedListing = Listing.builder().id("123").title("Kamera Update").build();
        when(listingService.updateListing(eq("123"), any(Listing.class))).thenReturn(updatedListing);

        String requestBody = objectMapper.writeValueAsString(updatedListing);

        mockMvc.perform(put("/api/v1/catalogue/listings/123")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Kamera Update"));
    }

    @Test
    void testUpdateListingEndpoint_NotFound() throws Exception {
        when(listingService.updateListing(eq("999"), any(Listing.class))).thenReturn(null);

        Listing updatedListing = Listing.builder().id("999").title("NotFound").build();
        String requestBody = objectMapper.writeValueAsString(updatedListing);

        mockMvc.perform(put("/api/v1/catalogue/listings/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isNotFound());
    }

    @Test
    void testDeleteListingEndpoint_Found() throws Exception {
        when(listingService.getListingById("123")).thenReturn(sampleListing);
        doNothing().when(listingService).deleteListing("123");

        mockMvc.perform(delete("/api/v1/catalogue/listings/123"))
                .andExpect(status().isNoContent());
    }

    @Test
    void testDeleteListingEndpoint_NotFound() throws Exception {
        when(listingService.getListingById("999")).thenReturn(null);

        mockMvc.perform(delete("/api/v1/catalogue/listings/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testSearchListingsEndpoint() throws Exception {
        when(listingService.searchListings(eq("Fotografi"), eq("Kamera"), any(), any(), eq("ACTIVE")))
                .thenReturn(Arrays.asList(sampleListing));

        mockMvc.perform(get("/api/v1/catalogue/listings/search")
                        .param("category", "Fotografi")
                        .param("keyword", "Kamera")
                        .param("status", "ACTIVE"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].title").value("Kamera Test"));
    }
}