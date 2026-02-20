package id.ac.ui.cs.advprog.bidmartcatalogueservice.service;

import id.ac.ui.cs.advprog.bidmartcatalogueservice.model.Listing;
import id.ac.ui.cs.advprog.bidmartcatalogueservice.repository.ListingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service // Penting agar Spring mengenali ini sebagai komponen Service
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
    public List<Listing> searchListings(String category, String keyword) {
        if (category != null && keyword != null) {
            // Logika pencarian gabungan (bisa dikembangkan di repository)
            return listingRepository.findByTitleContainingIgnoreCase(keyword);
        } else if (category != null) {
            return listingRepository.findByCategory(category);
        } else if (keyword != null) {
            return listingRepository.findByTitleContainingIgnoreCase(keyword);
        }
        return getAllListings();
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