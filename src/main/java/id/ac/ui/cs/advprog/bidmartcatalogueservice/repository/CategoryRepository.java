package id.ac.ui.cs.advprog.bidmartcatalogueservice.repository;

import id.ac.ui.cs.advprog.bidmartcatalogueservice.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
}
