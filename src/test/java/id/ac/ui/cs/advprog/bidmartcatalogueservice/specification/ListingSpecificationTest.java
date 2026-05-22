package id.ac.ui.cs.advprog.bidmartcatalogueservice.specification;

import id.ac.ui.cs.advprog.bidmartcatalogueservice.model.Listing;
import id.ac.ui.cs.advprog.bidmartcatalogueservice.model.ListingStatus;
import id.ac.ui.cs.advprog.bidmartcatalogueservice.repository.ListingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
class ListingSpecificationTest {

    @Autowired
    private ListingRepository listingRepository;

    @BeforeEach
    void setUp() {
        listingRepository.deleteAll();

        Listing item1 = Listing.builder()
                .title("Laptop Gaming Pro")
                .category("Elektronik")
                .currentPrice(new BigDecimal("15000"))
                .status(ListingStatus.ACTIVE)
                .endTime(LocalDateTime.now().plusDays(1))
                .build();

        Listing item2 = Listing.builder()
                .title("Keyboard Mechanical")
                .category("Elektronik")
                .currentPrice(new BigDecimal("1000"))
                .status(ListingStatus.DRAFT)
                .endTime(LocalDateTime.now().plusDays(3))
                .build();

        Listing item3 = Listing.builder()
                .title("Meja Kayu")
                .category("Furniture")
                .currentPrice(new BigDecimal("2500"))
                .status(ListingStatus.ACTIVE)
                .endTime(LocalDateTime.now().minusDays(1))
                .build();

        listingRepository.saveAll(List.of(item1, item2, item3));
    }

    @Test
    void testFilterByKeyword() {
        Specification<Listing> spec = ListingSpecification.filterListings(
                "gaming", null, null, null, null, null, null, null, null);
        List<Listing> result = listingRepository.findAll(spec);

        assertEquals(1, result.size());
        assertEquals("Laptop Gaming Pro", result.get(0).getTitle());
    }

    @Test
    void testFilterByCategory() {
        Specification<Listing> spec = ListingSpecification.filterListings(
                null, "Elektronik", null, null, null, null, null, null, null);
        List<Listing> result = listingRepository.findAll(spec);

        assertEquals(2, result.size());
    }

    @Test
    void testFilterByMinPrice() {
        Specification<Listing> spec = ListingSpecification.filterListings(
                null, null, null, new BigDecimal("2000"), null, null, null, null, null);
        List<Listing> result = listingRepository.findAll(spec);

        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(l -> l.getTitle().equals("Laptop Gaming Pro")));
        assertTrue(result.stream().anyMatch(l -> l.getTitle().equals("Meja Kayu")));
    }

    @Test
    void testFilterByMaxPrice() {
        Specification<Listing> spec = ListingSpecification.filterListings(
                null, null, null, null, new BigDecimal("2000"), null, null, null, null);
        List<Listing> result = listingRepository.findAll(spec);

        assertEquals(1, result.size());
        assertEquals("Keyboard Mechanical", result.get(0).getTitle());
    }

    @Test
    void testFilterByStatus() {
        Specification<Listing> spec = ListingSpecification.filterListings(
                null, null, null, null, null, ListingStatus.DRAFT, null, null, null);
        List<Listing> result = listingRepository.findAll(spec);

        assertEquals(1, result.size());
        assertEquals("Keyboard Mechanical", result.get(0).getTitle());
    }

    @Test
    void testFilterCombined() {
        Specification<Listing> spec = ListingSpecification.filterListings(
                "laptop",
                "Elektronik",
                null,
                new BigDecimal("10000"),
                new BigDecimal("20000"),
                ListingStatus.ACTIVE,
                null,
                null,
                null
        );
        List<Listing> result = listingRepository.findAll(spec);

        assertEquals(1, result.size());
        assertEquals("Laptop Gaming Pro", result.get(0).getTitle());
    }

    @Test
    void testFilterNoMatches() {
        Specification<Listing> spec = ListingSpecification.filterListings(
                "sepeda", "Olahraga", null, null, null, null, null, null, null);
        List<Listing> result = listingRepository.findAll(spec);

        assertTrue(result.isEmpty());
    }

    @Test
    void testFilterNullStatusIgnored() {
        Specification<Listing> spec = ListingSpecification.filterListings(
                "", "  ", null, null, null, null, null, null, null);
        List<Listing> result = listingRepository.findAll(spec);

        assertEquals(3, result.size());
    }

    @Test
    void testFilterByStatusesWhenSingleStatusAbsent() {
        Specification<Listing> spec = ListingSpecification.filterListings(
                null, null, null, null, null, null,
                List.of(ListingStatus.ACTIVE), null, null);
        List<Listing> result = listingRepository.findAll(spec);

        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(listing -> listing.getStatus() == ListingStatus.ACTIVE));
    }

    @Test
    void testSingleStatusTakesPrecedenceOverStatusesList() {
        Specification<Listing> spec = ListingSpecification.filterListings(
                null, null, null, null, null, ListingStatus.DRAFT,
                List.of(ListingStatus.ACTIVE), null, null);
        List<Listing> result = listingRepository.findAll(spec);

        assertEquals(1, result.size());
        assertEquals("Keyboard Mechanical", result.get(0).getTitle());
    }

    @Test
    void testFilterByEndBefore() {
        Specification<Listing> spec = ListingSpecification.filterListings(
                null, null, null, null, null, null, null,
                LocalDateTime.now().plusDays(2), null);
        List<Listing> result = listingRepository.findAll(spec);

        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(l -> l.getTitle().equals("Laptop Gaming Pro")));
        assertTrue(result.stream().anyMatch(l -> l.getTitle().equals("Meja Kayu")));
    }

    @Test
    void testFilterByEndAfter() {
        Specification<Listing> spec = ListingSpecification.filterListings(
                null, null, null, null, null, null, null,
                null, LocalDateTime.now().plusDays(2));
        List<Listing> result = listingRepository.findAll(spec);

        assertEquals(1, result.size());
        assertEquals("Keyboard Mechanical", result.get(0).getTitle());
    }
}
