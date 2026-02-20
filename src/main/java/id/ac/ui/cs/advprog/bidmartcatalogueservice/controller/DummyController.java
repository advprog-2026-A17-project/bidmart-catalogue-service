package id.ac.ui.cs.advprog.bidmartcatalogueservice.controller;

import id.ac.ui.cs.advprog.bidmartcatalogueservice.model.Listing;
import id.ac.ui.cs.advprog.bidmartcatalogueservice.service.ListingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class DummyController {

    @Autowired
    private ListingService listingService;

    @GetMapping("/")
    public String home(Model model) {
        // Mengambil semua data dari database
        List<Listing> allListings = listingService.getAllListings();

        // mengirim data tersebut ke file HTML dengan nama variabel "listings"
        model.addAttribute("title", "Katalog BidMart");
        model.addAttribute("listings", allListings);

        return "index"; //  ke file index.html
    }
}
// Note: untuk menjalankan dummy controller terjadi perubahan pada controller terutama rest controller dan auto wired juga di comment
// Di bagian service juga melakukan komen pada service dan autowired
// terdapat sedikit perubahan juga pada bidmart catalogue service application dan application.properties juga