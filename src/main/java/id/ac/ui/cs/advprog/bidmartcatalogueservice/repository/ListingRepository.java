package id.ac.ui.cs.advprog.bidmartcatalogueservice.repository;

import id.ac.ui.cs.advprog.bidmartcatalogueservice.model.Listing;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ListingRepository extends JpaRepository<Listing, String>, JpaSpecificationExecutor<Listing> { // Tambahkan ini
    List<Listing> findByCategory(String category);
    List<Listing> findByTitleContainingIgnoreCase(String keyword);
    List<Listing> findBySellerId(String sellerId);
}