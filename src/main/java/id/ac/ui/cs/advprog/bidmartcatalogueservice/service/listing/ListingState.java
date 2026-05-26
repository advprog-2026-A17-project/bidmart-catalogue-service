package id.ac.ui.cs.advprog.bidmartcatalogueservice.service.listing;

import id.ac.ui.cs.advprog.bidmartcatalogueservice.model.Listing;
import id.ac.ui.cs.advprog.bidmartcatalogueservice.model.ListingStatus;

import java.math.BigDecimal;

public interface ListingState {

    default void publish(Listing listing) {
        throw new IllegalStateException("Only DRAFT listings can be published, current status: " + listing.getStatus());
    }

    default void deactivate(Listing listing) {
        throw new IllegalStateException("Only ACTIVE listings can be deactivated, current status: " + listing.getStatus());
    }

    default void markExtended(Listing listing) {
        throw new IllegalStateException("Only ACTIVE or EXTENDED listings can be marked as EXTENDED, current status: " + listing.getStatus());
    }

    default void markClosed(Listing listing) {
        throw new IllegalStateException("Only ACTIVE or EXTENDED listings can be marked as CLOSED, current status: " + listing.getStatus());
    }

    default void markWon(Listing listing, BigDecimal finalPrice) {
        throw new IllegalStateException("Only ACTIVE, EXTENDED, or CLOSED listings can be marked as WON, current status: " + listing.getStatus());
    }

    default void markUnsold(Listing listing) {
        throw new IllegalStateException("Only ACTIVE, EXTENDED, or CLOSED listings can be marked as UNSOLD, current status: " + listing.getStatus());
    }

    default void adminClose(Listing listing) {
        listing.setStatus(ListingStatus.CANCELLED);
    }

    default void cancel(Listing listing) {
        if (listing.isHasBids()) {
            throw new IllegalStateException("Listing has active bids");
        }
        listing.setStatus(ListingStatus.CANCELLED);
    }
}
