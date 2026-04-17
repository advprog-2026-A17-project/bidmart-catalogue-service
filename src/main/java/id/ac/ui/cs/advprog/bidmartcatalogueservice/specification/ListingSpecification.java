package id.ac.ui.cs.advprog.bidmartcatalogueservice.specification;

import id.ac.ui.cs.advprog.bidmartcatalogueservice.model.Listing;
import org.springframework.data.jpa.domain.Specification;
import jakarta.persistence.criteria.Predicate;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class ListingSpecification {
    public static Specification<Listing> filterListings(String keyword, String category, BigDecimal minPrice, BigDecimal maxPrice, String status) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (keyword != null && !keyword.trim().isEmpty()) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("title")), "%" + keyword.toLowerCase() + "%"));
            }
            if (category != null && !category.trim().isEmpty()) {
                predicates.add(criteriaBuilder.equal(root.get("category"), category));
            }
            if (minPrice != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("currentPrice"), minPrice));
            }
            if (maxPrice != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("currentPrice"), maxPrice));
            }
            if (status != null && !status.trim().isEmpty()) {
                predicates.add(criteriaBuilder.equal(root.get("status"), status));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}