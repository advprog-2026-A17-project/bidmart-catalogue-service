package id.ac.ui.cs.advprog.bidmartcatalogueservice.config;

import id.ac.ui.cs.advprog.bidmartcatalogueservice.model.Listing;
import id.ac.ui.cs.advprog.bidmartcatalogueservice.model.ListingStatus;
import id.ac.ui.cs.advprog.bidmartcatalogueservice.repository.ListingRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Configuration
public class DataSeeder {

    @Bean
    CommandLineRunner initDatabase(ListingRepository repository) {
        return args -> {
            // Cek apakah database masih kosong
            if (repository.count() == 0) {
                Listing item1 = Listing.builder()
                        .sellerId("seller-001")
                        .title("Laptop Gaming ROG Strix")
                        .description("Laptop bekas pemakaian 1 tahun, kondisi masih 95% mulus.")
                        .category("Elektronik")
                        .imageUrl("https://dummyimage.com/400x400/000/fff&text=ROG+Strix")
                        .startingPrice(new BigDecimal("15000000"))
                        .reservePrice(new BigDecimal("17000000"))
                        .currentPrice(new BigDecimal("15000000"))
                        .endTime(LocalDateTime.now().plusDays(7))
                        .status(ListingStatus.ACTIVE)
                        .build();

                Listing item2 = Listing.builder()
                        .sellerId("seller-002")
                        .title("Sepeda Lipat Brompton")
                        .description("Sepeda lipat original, jarang dipakai.")
                        .category("Olahraga")
                        .imageUrl("https://dummyimage.com/400x400/000/fff&text=Brompton")
                        .startingPrice(new BigDecimal("25000000"))
                        .reservePrice(new BigDecimal("28000000"))
                        .currentPrice(new BigDecimal("25000000"))
                        .endTime(LocalDateTime.now().plusDays(3))
                        .status(ListingStatus.ACTIVE)
                        .build();

                Listing item3 = Listing.builder()
                        .sellerId("seller-003")
                        .title("Kamera Sony A7 III")
                        .description("Kamera mirrorless full frame beserta lensa kit.")
                        .category("Fotografi")
                        .imageUrl("https://dummyimage.com/400x400/000/fff&text=Sony+A7III")
                        .startingPrice(new BigDecimal("18000000"))
                        .reservePrice(new BigDecimal("20000000"))
                        .currentPrice(new BigDecimal("18000000"))
                        .endTime(LocalDateTime.now().plusDays(5))
                        .status(ListingStatus.ACTIVE)
                        .build();

                // Simpan ke database
                repository.saveAll(List.of(item1, item2, item3));
                System.out.println("Data Seeding: Berhasil memasukkan 3 produk katalog ke dalam database!");
            } else {
                System.out.println("Data Seeding: Database sudah berisi data, seeding dilewati.");
            }
        };
    }
}