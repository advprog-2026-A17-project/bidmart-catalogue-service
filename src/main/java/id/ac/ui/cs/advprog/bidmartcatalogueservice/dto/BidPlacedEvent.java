package id.ac.ui.cs.advprog.bidmartcatalogueservice.dto;

import java.math.BigDecimal;

public record BidPlacedEvent(String listingId, BigDecimal amount) {}
