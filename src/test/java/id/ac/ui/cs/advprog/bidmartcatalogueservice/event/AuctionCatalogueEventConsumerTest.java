package id.ac.ui.cs.advprog.bidmartcatalogueservice.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import id.ac.ui.cs.advprog.bidmartcatalogueservice.model.ListingStatus;
import id.ac.ui.cs.advprog.bidmartcatalogueservice.service.ListingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AuctionCatalogueEventConsumerTest {

    @Mock
    private ListingService listingService;

    private AuctionCatalogueEventConsumer consumer;

    @BeforeEach
    void setUp() {
        consumer = new AuctionCatalogueEventConsumer(new ObjectMapper(), listingService);
    }

    @Test
    void bidPlacedEventUpdatesListingPriceAndHasBids() throws Exception {
        consumer.consume("""
                {
                  "eventId": "evt-bid-1",
                  "eventType": "auction.bid-placed",
                  "eventVersion": 1,
                  "aggregateId": "auction-1",
                    "payload": {
                    "listingId": "listing-1",
                    "currentPrice": 12500,
                    "status": "EXTENDED",
                    "endTime": 1767225600
                  }
                }
                """);

        verify(listingService).synchronizeBidState(
                "listing-1",
                new BigDecimal("125.00"),
                ListingStatus.EXTENDED,
                LocalDateTime.of(2026, 1, 1, 0, 0)
        );
    }

    @Test
    void auctionEndedWithWonStatusMarksListingWon() throws Exception {
        consumer.consume("""
                {
                  "eventId": "evt-ended-1",
                  "eventType": "auction.ended",
                  "eventVersion": 1,
                  "aggregateId": "auction-1",
                    "payload": {
                    "listingId": "listing-1",
                    "status": "WON",
                    "winnerId": "buyer-1",
                    "finalPrice": 14000
                  }
                }
                """);

        verify(listingService).markWon("listing-1", new BigDecimal("140.00"));
    }

    @Test
    void auctionEndedWithWinnerButMissingStatusMarksListingUnsold() throws Exception {
        consumer.consume("""
                {
                  "eventId": "evt-ended-legacy",
                  "eventType": "auction.ended",
                  "eventVersion": 1,
                  "aggregateId": "auction-legacy",
                    "payload": {
                    "listingId": "listing-legacy",
                    "winnerId": "buyer-1",
                    "finalPrice": 14000
                  }
                }
                """);

        verify(listingService).markUnsold("listing-legacy");
        verify(listingService, never()).markWon(org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.any());
    }

    @Test
    void auctionEndedBelowReserveMarksListingUnsoldEvenWithLegacyWinnerId() throws Exception {
        consumer.consume("""
                {
                  "eventId": "evt-ended-3",
                  "eventType": "auction.ended",
                  "eventVersion": 1,
                  "aggregateId": "auction-3",
                  "payload": {
                    "listingId": "listing-3",
                    "status": "UNSOLD",
                    "winnerId": "buyer-1",
                    "finalPrice": 4000
                  }
                }
                """);

        verify(listingService).markUnsold("listing-3");
        verify(listingService, never()).markWon(org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.any());
    }

    @Test
    void auctionEndedWithoutWinnerMarksListingUnsold() throws Exception {
        consumer.consume("""
                {
                  "eventId": "evt-ended-2",
                  "eventType": "auction.ended",
                  "eventVersion": 1,
                  "aggregateId": "auction-2",
                  "payload": {
                    "listingId": "listing-2",
                    "finalPrice": 0
                  }
                }
                """);

        verify(listingService).markUnsold("listing-2");
    }

    @Test
    void duplicateEventIdIsIgnored() throws Exception {
        String event = """
                {
                  "eventId": "evt-bid-duplicate",
                  "eventType": "auction.bid-placed",
                  "eventVersion": 1,
                  "aggregateId": "auction-1",
                  "payload": {
                    "listingId": "listing-1",
                    "currentPrice": 12500
                  }
                }
                """;

        consumer.consume(event);
        consumer.consume(event);

        verify(listingService, times(1)).synchronizeBidState(
                "listing-1",
                new BigDecimal("125.00"),
                null,
                null
        );
    }
}
