package id.ac.ui.cs.advprog.bidmartcatalogueservice.controller;

import id.ac.ui.cs.advprog.bidmartcatalogueservice.model.Item;
import id.ac.ui.cs.advprog.bidmartcatalogueservice.service.CatalogueService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/catalogue/listings")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class CatalogueController {

    private final CatalogueService catalogueService;

    @PostMapping
    public ResponseEntity<Item> createListing(@RequestBody Item item) {
        return ResponseEntity.ok(catalogueService.createListing(item));
    }

    // pencarian dengan parameter (contoh: /search?minPrice=10000&keyword=iphone)
    @GetMapping("/search")
    public ResponseEntity<List<Item>> searchListings(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) String keyword) {
        return ResponseEntity.ok(catalogueService.searchListings(categoryId, minPrice, maxPrice, keyword));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Item> getListingDetail(@PathVariable Long id) {
        return ResponseEntity.ok(catalogueService.getListingDetail(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Item> updateListing(@PathVariable Long id, @RequestBody Item item) {
        return ResponseEntity.ok(catalogueService.updateListing(id, item));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> cancelListing(@PathVariable Long id) {
        catalogueService.cancelListing(id);
        return ResponseEntity.ok("Listing berhasil dibatalkan");
    }

    // endpoint khusus untuk kebutuhan synchronous dari modul lelang
    @GetMapping("/{id}/validate")
    public ResponseEntity<Boolean> validateListing(@PathVariable Long id) {
        return ResponseEntity.ok(catalogueService.validateListingActive(id));
    }
}