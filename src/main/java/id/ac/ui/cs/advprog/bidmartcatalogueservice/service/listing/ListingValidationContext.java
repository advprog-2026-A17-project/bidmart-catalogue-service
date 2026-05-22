package id.ac.ui.cs.advprog.bidmartcatalogueservice.service.listing;

import id.ac.ui.cs.advprog.bidmartcatalogueservice.model.Listing;
import id.ac.ui.cs.advprog.bidmartcatalogueservice.repository.CategoryRepository;

import java.time.LocalDateTime;

public record ListingValidationContext(
        Listing listing,
        Listing existingListing,
        CategoryRepository categoryRepository,
        LocalDateTime now,
        boolean validateSchedule,
        boolean resolveCategory
) {
}
