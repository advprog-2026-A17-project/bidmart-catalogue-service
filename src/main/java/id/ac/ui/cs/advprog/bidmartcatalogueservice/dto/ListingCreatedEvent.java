package id.ac.ui.cs.advprog.bidmartcatalogueservice.dto;

import id.ac.ui.cs.advprog.bidmartcatalogueservice.model.ListingStatus;

import java.math.BigDecimal;

public record ListingCreatedEvent(String listingId, String sellerId, BigDecimal startingPrice, ListingStatus status) {}
