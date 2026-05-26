package id.ac.ui.cs.advprog.bidmartcatalogueservice.service.listing;

import id.ac.ui.cs.advprog.bidmartcatalogueservice.model.Listing;
import id.ac.ui.cs.advprog.bidmartcatalogueservice.model.ListingStatus;

import java.math.BigDecimal;

public interface ListingExpiryStrategy {

    ListingStatus resolveExpiredStatus(Listing listing);

    static ListingExpiryStrategy forListing(Listing listing) {
        return new AuctionListingExpiryStrategy();
    }
}

final class AuctionListingExpiryStrategy implements ListingExpiryStrategy {
    @Override
    public ListingStatus resolveExpiredStatus(Listing listing) {
        if (!listing.isHasBids()) {
            return ListingStatus.UNSOLD;
        }
        BigDecimal reservePrice = listing.getReservePrice();
        BigDecimal currentPrice = listing.getCurrentPrice();
        if (reservePrice != null && currentPrice != null && currentPrice.compareTo(reservePrice) < 0) {
            return ListingStatus.UNSOLD;
        }
        return ListingStatus.CLOSED;
    }
}
