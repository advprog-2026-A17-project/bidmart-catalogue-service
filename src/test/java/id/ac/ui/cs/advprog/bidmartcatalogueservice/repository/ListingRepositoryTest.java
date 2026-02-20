package id.ac.ui.cs.advprog.bidmartcatalogueservice.repository;

import id.ac.ui.cs.advprog.bidmartcatalogueservice.model.Listing;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

@DataJpaTest
class ListingRepositoryTest {

    @Autowired
    private ListingRepository listingRepository;

    @BeforeEach
    void setUp() {
        Listing item1 = Listing.builder()
                .title("Laptop Gaming")
                .category("Elektronik")
                .startingPrice(new BigDecimal("10000"))
                .build();

        Listing item2 = Listing.builder()
                .title("Mouse Gaming")
                .category("Elektronik")
                .startingPrice(new BigDecimal("500"))
                .build();

        Listing item3 = Listing.builder()
                .title("Sepeda Lipat")
                .category("Olahraga")
                .startingPrice(new BigDecimal("5000"))
                .build();

        listingRepository.saveAll(List.of(item1, item2, item3));
    }

    @Test
    void testFindByCategory() {
        List<Listing> elektronikList = listingRepository.findByCategory("Elektronik");

        assertFalse(elektronikList.isEmpty());
        assertEquals(2, elektronikList.size());
    }

    @Test
    void testFindByTitleContainingIgnoreCase() {
        List<Listing> searchResult = listingRepository.findByTitleContainingIgnoreCase("gaming");

        assertFalse(searchResult.isEmpty());
        assertEquals(2, searchResult.size());
    }

    @Test
    void testSaveAndFindById() {
        Listing newItem = Listing.builder()
                .title("Kamera Baru")
                .category("Fotografi")
                .build();

        Listing savedItem = listingRepository.save(newItem);
        Listing foundItem = listingRepository.findById(savedItem.getId()).orElse(null);

        assertEquals("Kamera Baru", foundItem.getTitle());
    }
}