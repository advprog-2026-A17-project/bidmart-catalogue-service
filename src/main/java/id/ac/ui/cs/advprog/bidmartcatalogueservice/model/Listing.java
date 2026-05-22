package id.ac.ui.cs.advprog.bidmartcatalogueservice.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "listings")
@Getter
@Setter
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
    @JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private Category categoryEntity;

    private BigDecimal startingPrice;
    private BigDecimal reservePrice;
    private BigDecimal currentPrice; // update harga dari modul lelang
    private BigDecimal minimumIncrement;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    private LocalDateTime startTime;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    private LocalDateTime endTime;

    @Enumerated(EnumType.STRING)
    private ListingStatus status; // DRAFT, ACTIVE, EXTENDED, CLOSED, WON, UNSOLD, CANCELLED

    @Builder.Default
    private boolean hasBids = false;
}
