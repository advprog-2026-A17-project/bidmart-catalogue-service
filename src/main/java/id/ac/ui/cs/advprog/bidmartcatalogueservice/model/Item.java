package id.ac.ui.cs.advprog.bidmartcatalogueservice.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "items")
@Data
public class Item {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String imageUrl;

    @Column(nullable = false)
    private Double startingPrice;

    private Double reservePrice;

    @Column(nullable = false)
    private Double currentPrice;

    @Column(nullable = false)
    private LocalDateTime endTime;

    @Column(nullable = false)
    private String sellerId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "category_id")
    private Category category;

    @Column(nullable = false)
    private String status = "ACTIVE";

    @Column(nullable = false)
    private boolean hasBids = false;
}