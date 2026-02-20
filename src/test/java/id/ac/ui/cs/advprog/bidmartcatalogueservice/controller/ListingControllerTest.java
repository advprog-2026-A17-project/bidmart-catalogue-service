package id.ac.ui.cs.advprog.bidmartcatalogueservice.controller;

import id.ac.ui.cs.advprog.bidmartcatalogueservice.model.Listing;
import id.ac.ui.cs.advprog.bidmartcatalogueservice.service.ListingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ListingController.class)
class ListingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ListingService listingService;

    private Listing sampleListing;

    @BeforeEach
    void setUp() {
        sampleListing = Listing.builder()
                .id("123")
                .title("Kamera Test")
                .category("Fotografi")
                .build();
    }

    @Test
    void testGetAllListingsEndpoint() throws Exception {
        when(listingService.getAllListings()).thenReturn(Arrays.asList(sampleListing));

        mockMvc.perform(get("/api/v1/listings"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].id").value("123"))
                .andExpect(jsonPath("$[0].title").value("Kamera Test"));
    }

    @Test
    void testGetListingByIdEndpoint() throws Exception {
        when(listingService.getListingById("123")).thenReturn(sampleListing);

        mockMvc.perform(get("/api/v1/listings/123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Kamera Test"));
    }

    @Test
    void testCreateListingEndpoint() throws Exception {
        when(listingService.createListing(any(Listing.class))).thenReturn(sampleListing);

        // JSON body palsu untuk testing
        String requestBody = "{\"title\":\"Kamera Test\",\"category\":\"Fotografi\"}";

        mockMvc.perform(post("/api/v1/listings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Kamera Test"));
    }
}