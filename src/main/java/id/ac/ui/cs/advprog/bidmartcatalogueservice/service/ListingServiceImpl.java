package id.ac.ui.cs.advprog.bidmartcatalogueservice.service;

import id.ac.ui.cs.advprog.bidmartcatalogueservice.model.Listing;
import id.ac.ui.cs.advprog.bidmartcatalogueservice.repository.ListingRepository;
import id.ac.ui.cs.advprog.bidmartcatalogueservice.specification.ListingSpecification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class ListingServiceImpl implements ListingService {

    @Autowired
    private ListingRepository listingRepository;

    @Override
    public Listing createListing(Listing listing) {
        // Set status awal sesuai kebutuhan modul katalog
        if (listing.getStatus() == null) {
            listing.setStatus("ACTIVE");
        }
        return listingRepository.save(listing);
    }

    @Override
    public Listing getListingById(String id) {
        return listingRepository.findById(id).orElse(null);
    }

    @Override
    public List<Listing> getAllListings() {
        return listingRepository.findAll();
    }

    @Override
    public List<Listing> searchListings(String category, String keyword, BigDecimal minPrice, BigDecimal maxPrice, String status) {
        // Gunakan dynamic filtering
        Specification<Listing> spec = ListingSpecification.filterListings(keyword, category, minPrice, maxPrice, status);
        return listingRepository.findAll(spec);
    }

    @Override
    public Listing updateListing(String id, Listing listing) {
        if (listingRepository.existsById(id)) {
            listing.setId(id);
            return listingRepository.save(listing);
        }
        return null;
    }

    @Override
    public void deleteListing(String id) {
        listingRepository.deleteById(id);
    }
}