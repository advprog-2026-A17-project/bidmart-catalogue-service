package id.ac.ui.cs.advprog.bidmartcatalogueservice.dto;

import id.ac.ui.cs.advprog.bidmartcatalogueservice.model.ListingStatus;

public record ListingClosedByAdminEvent(String listingId, String sellerId, ListingStatus status, String reason) {
}
