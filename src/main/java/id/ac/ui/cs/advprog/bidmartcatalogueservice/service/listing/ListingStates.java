package id.ac.ui.cs.advprog.bidmartcatalogueservice.service.listing;

import id.ac.ui.cs.advprog.bidmartcatalogueservice.model.Listing;
import id.ac.ui.cs.advprog.bidmartcatalogueservice.model.ListingStatus;

import java.math.BigDecimal;

public final class ListingStates {

    private static final ListingState DRAFT = new DraftListingState();
    private static final ListingState ACTIVE = new ActiveListingState();
    private static final ListingState EXTENDED = new ExtendedListingState();
    private static final ListingState CLOSED = new ClosedListingState();
    private static final ListingState TERMINAL = new TerminalListingState();

    private ListingStates() {
    }

    public static ListingState forStatus(ListingStatus status) {
        return switch (status) {
            case DRAFT -> DRAFT;
            case ACTIVE -> ACTIVE;
            case EXTENDED -> EXTENDED;
            case CLOSED -> CLOSED;
            case WON, UNSOLD, CANCELLED -> TERMINAL;
        };
    }

    private static final class DraftListingState implements ListingState {
        @Override
        public void publish(Listing listing) {
            listing.setStatus(ListingStatus.ACTIVE);
        }
    }

    private static class ActiveListingState implements ListingState {
        @Override
        public void deactivate(Listing listing) {
            if (listing.isHasBids()) {
                throw new IllegalStateException("Cannot deactivate listing with active bids");
            }
            listing.setStatus(ListingStatus.DRAFT);
        }

        @Override
        public void markExtended(Listing listing) {
            listing.setStatus(ListingStatus.EXTENDED);
        }

        @Override
        public void markClosed(Listing listing) {
            listing.setStatus(ListingStatus.CLOSED);
        }

        @Override
        public void markWon(Listing listing, BigDecimal finalPrice) {
            listing.setStatus(ListingStatus.WON);
            listing.setCurrentPrice(finalPrice);
        }

        @Override
        public void markUnsold(Listing listing) {
            listing.setStatus(ListingStatus.UNSOLD);
        }
    }

    private static final class ExtendedListingState extends ActiveListingState {
    }

    private static final class ClosedListingState implements ListingState {
        @Override
        public void cancel(Listing listing) {
            if (listing.isHasBids()) {
                throw new IllegalStateException("Listing has active bids");
            }
            throw new IllegalStateException("Cannot cancel listing with status: " + listing.getStatus());
        }

        @Override
        public void markWon(Listing listing, BigDecimal finalPrice) {
            listing.setStatus(ListingStatus.WON);
            listing.setCurrentPrice(finalPrice);
        }

        @Override
        public void markUnsold(Listing listing) {
            listing.setStatus(ListingStatus.UNSOLD);
        }
    }

    private static final class TerminalListingState implements ListingState {
        @Override
        public void adminClose(Listing listing) {
            throw new IllegalStateException("Cannot admin-close listing with status: " + listing.getStatus());
        }

        @Override
        public void cancel(Listing listing) {
            if (listing.isHasBids()) {
                throw new IllegalStateException("Listing has active bids");
            }
            throw new IllegalStateException("Cannot cancel listing with status: " + listing.getStatus());
        }
    }
}
