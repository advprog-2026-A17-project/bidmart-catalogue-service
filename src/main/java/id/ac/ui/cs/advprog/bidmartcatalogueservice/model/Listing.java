package id.ac.ui.cs.advprog.bidmartcatalogueservice.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
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
    @Column(columnDefinition = "TEXT")
    private String description;
    private String category;
    private String condition;

    @Column(columnDefinition = "TEXT")
    private String imageUrl;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "category_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Category categoryEntity;

    private BigDecimal startingPrice;
    private BigDecimal reservePrice;
    private BigDecimal currentPrice; //  update harga dari modul lelang

    private LocalDateTime endTime;

    private String condition; 

    @Enumerated(EnumType.STRING)
    private ListingStatus status; // DRAFT, ACTIVE, AUCTION_CREATED, SOLD, UNSOLD, CANCELLED

    @Builder.Default
    private boolean hasBids = false;
}
