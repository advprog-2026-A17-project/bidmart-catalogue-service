package id.ac.ui.cs.advprog.bidmartcatalogueservice.dto;

import java.math.BigDecimal;

public record ListingCreatedEvent(String listingId, String sellerId, BigDecimal startingPrice, String status) {}
