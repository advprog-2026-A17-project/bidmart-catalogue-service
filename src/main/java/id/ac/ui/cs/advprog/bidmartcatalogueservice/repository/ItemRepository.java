package id.ac.ui.cs.advprog.bidmartcatalogueservice.repository;

import id.ac.ui.cs.advprog.bidmartcatalogueservice.model.Item;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ItemRepository extends JpaRepository<Item, Long> {

    @Query("SELECT i FROM Item i WHERE i.status = 'ACTIVE' " +
            "AND i.endTime > :currentTime " +
            "AND (:categoryId IS NULL OR i.category.id = :categoryId) " +
            "AND (:minPrice IS NULL OR i.currentPrice >= :minPrice) " +
            "AND (:maxPrice IS NULL OR i.currentPrice <= :maxPrice) " +
            "AND LOWER(i.title) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Item> searchListings(
            @Param("categoryId") Long categoryId,
            @Param("minPrice") Double minPrice,
            @Param("maxPrice") Double maxPrice,
            @Param("keyword") String keyword,
            @Param("currentTime") LocalDateTime currentTime
    );
}