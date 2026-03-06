package id.ac.ui.cs.advprog.bidmartcatalogueservice.service;

import id.ac.ui.cs.advprog.bidmartcatalogueservice.model.Item;
import id.ac.ui.cs.advprog.bidmartcatalogueservice.repository.ItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CatalogueService {

    private final ItemRepository itemRepository;

    public Item createListing(Item item) {
        item.setCurrentPrice(item.getStartingPrice());
        item.setStatus("ACTIVE");
        item.setHasBids(false);
        return itemRepository.save(item);
    }

    public List<Item> searchListings(Long categoryId, Double minPrice, Double maxPrice, String keyword) {
        String safeKeyword = (keyword == null) ? "" : keyword;
        return itemRepository.searchListings(categoryId, minPrice, maxPrice, safeKeyword, LocalDateTime.now());
    }

    public Item getListingDetail(Long id) {
        return itemRepository.findById(id).orElseThrow(() -> new RuntimeException("Listing tidak ditemukan"));
    }

    public Item updateListing(Long id, Item updatedItem) {
        Item existingItem = getListingDetail(id);
        if (existingItem.isHasBids()) {
            throw new RuntimeException("Tidak dapat mengubah listing karena sudah ada penawaran.");
        }
        existingItem.setDescription(updatedItem.getDescription());
        existingItem.setImageUrl(updatedItem.getImageUrl());
        return itemRepository.save(existingItem);
    }

    public void cancelListing(Long id) {
        Item existingItem = getListingDetail(id);
        if (existingItem.isHasBids()) {
            throw new RuntimeException("Tidak dapat membatalkan listing karena sudah ada penawaran.");
        }
        existingItem.setStatus("CANCELLED");
        itemRepository.save(existingItem);
    }


    // 1. synchronous api call: modul lelang ngecek apakah barang ini masih valid
    public boolean validateListingActive(Long id) {
        Item item = getListingDetail(id);
        return item.getStatus().equals("ACTIVE") && item.getEndTime().isAfter(LocalDateTime.now());
    }

    // 2. asynchronous (RabbitMQ/Kafka): update harga ketika modul lelang kirim pesan
    public void updatePriceFromBid(Long id, Double newPrice) {
        Item item = getListingDetail(id);
        item.setCurrentPrice(newPrice);
        item.setHasBids(true); // kunci barang agar tidak bisa diedit lagi
        itemRepository.save(item);
    }
}