package id.ac.ui.cs.advprog.bidmartcatalogueservice.controller;

import id.ac.ui.cs.advprog.bidmartcatalogueservice.service.CatalogueService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class WebCatalogueController {

    private final CatalogueService catalogueService;

    @GetMapping("/katalog")
    public String tampilkanKatalog(Model model) {
        var daftarBarang = catalogueService.searchListings(null, null, null, null);

        model.addAttribute("title", "Katalog Laptop & Barang Lelang");
        model.addAttribute("listings", daftarBarang);
        return "index";
    }
}
