package id.ac.ui.cs.advprog.bidmartcatalogueservice.model;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "listings")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Listing {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private String sellerId;
    private String title;
    private String description;
    private String category;
    private String imageUrl;

    private BigDecimal startingPrice;
    private BigDecimal reservePrice;
    private BigDecimal currentPrice; //  update harga dari modul lelang

    private LocalDateTime endTime;
    private String status; // DRAFT, ACTIVE, SOLD, CANCELLED

    @Builder.Default
    private boolean hasBids = false;
}
