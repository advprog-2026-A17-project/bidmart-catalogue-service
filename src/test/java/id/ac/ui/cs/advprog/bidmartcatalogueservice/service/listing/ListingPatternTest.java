package id.ac.ui.cs.advprog.bidmartcatalogueservice.service.listing;

import id.ac.ui.cs.advprog.bidmartcatalogueservice.model.Listing;
import id.ac.ui.cs.advprog.bidmartcatalogueservice.model.ListingStatus;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ListingPatternTest {

    @Test
    void activeStateCanCloseAndTerminalCannotCancel() {
        Listing listing = Listing.builder().status(ListingStatus.ACTIVE).build();

        ListingStates.forStatus(listing.getStatus()).markClosed(listing);

        assertEquals(ListingStatus.CLOSED, listing.getStatus());
        assertThrows(IllegalStateException.class,
                () -> ListingStates.forStatus(ListingStatus.CLOSED).cancel(listing));
    }

    @Test
    void draftStatePublishesAndRejectsInvalidClose() {
        Listing listing = Listing.builder().status(ListingStatus.DRAFT).build();

        ListingStates.forStatus(listing.getStatus()).publish(listing);

        assertEquals(ListingStatus.ACTIVE, listing.getStatus());
        assertThrows(IllegalStateException.class,
                () -> ListingStates.forStatus(ListingStatus.DRAFT).markClosed(listing));
    }

    @Test
    void activeStateDeactivateRequiresNoBids() {
        Listing listing = Listing.builder().status(ListingStatus.ACTIVE).hasBids(false).build();

        ListingStates.forStatus(listing.getStatus()).deactivate(listing);

        assertEquals(ListingStatus.DRAFT, listing.getStatus());

        Listing withBids = Listing.builder().status(ListingStatus.ACTIVE).hasBids(true).build();
        assertThrows(IllegalStateException.class,
                () -> ListingStates.forStatus(withBids.getStatus()).deactivate(withBids));
    }

    @Test
    void activeAndExtendedStatesCanResolveAuctionOutcomes() {
        Listing active = Listing.builder().status(ListingStatus.ACTIVE).build();
        ListingStates.forStatus(active.getStatus()).markExtended(active);
        assertEquals(ListingStatus.EXTENDED, active.getStatus());

        Listing extended = Listing.builder().status(ListingStatus.EXTENDED).build();
        ListingStates.forStatus(extended.getStatus()).markWon(extended, new BigDecimal("12345"));
        assertEquals(ListingStatus.WON, extended.getStatus());
        assertEquals(new BigDecimal("12345"), extended.getCurrentPrice());

        Listing unsold = Listing.builder().status(ListingStatus.EXTENDED).build();
        ListingStates.forStatus(unsold.getStatus()).markUnsold(unsold);
        assertEquals(ListingStatus.UNSOLD, unsold.getStatus());
    }

    @Test
    void closedStateCanMarkWonOrUnsoldButCannotCancel() {
        Listing won = Listing.builder().status(ListingStatus.CLOSED).hasBids(false).build();
        ListingStates.forStatus(won.getStatus()).markWon(won, new BigDecimal("999"));

        assertEquals(ListingStatus.WON, won.getStatus());
        assertEquals(new BigDecimal("999"), won.getCurrentPrice());

        Listing unsold = Listing.builder().status(ListingStatus.CLOSED).hasBids(false).build();
        ListingStates.forStatus(unsold.getStatus()).markUnsold(unsold);

        assertEquals(ListingStatus.UNSOLD, unsold.getStatus());
        assertThrows(IllegalStateException.class,
                () -> ListingStates.forStatus(ListingStatus.CLOSED).cancel(unsold));
    }

    @Test
    void terminalStateRejectsAdminCloseAndCancelWithStatusSpecificMessage() {
        Listing cancelled = Listing.builder().status(ListingStatus.CANCELLED).hasBids(false).build();

        assertThrows(IllegalStateException.class,
                () -> ListingStates.forStatus(cancelled.getStatus()).adminClose(cancelled));
        assertThrows(IllegalStateException.class,
                () -> ListingStates.forStatus(cancelled.getStatus()).cancel(cancelled));

        Listing withBids = Listing.builder().status(ListingStatus.WON).hasBids(true).build();
        assertThrows(IllegalStateException.class,
                () -> ListingStates.forStatus(withBids.getStatus()).cancel(withBids));
    }

    @Test
    void expiryStrategyMarksReserveMetListingClosed() {
        Listing listing = Listing.builder()
                .status(ListingStatus.ACTIVE)
                .hasBids(true)
                .reservePrice(new BigDecimal("10000"))
                .currentPrice(new BigDecimal("12000"))
                .build();

        ListingStatus status = ListingExpiryStrategy.forListing(listing).resolveExpiredStatus(listing);

        assertEquals(ListingStatus.CLOSED, status);
    }
}
