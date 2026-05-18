package id.ac.ui.cs.advprog.bidmartcatalogueservice.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import id.ac.ui.cs.advprog.bidmartcatalogueservice.service.ListingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

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
                    "currentPrice": 12500
                  }
                }
                """);

        verify(listingService).handleBidPlaced("listing-1", new BigDecimal("12500"));
    }

    @Test
    void auctionEndedWithWinnerMarksListingWon() throws Exception {
        consumer.consume("""
                {
                  "eventId": "evt-ended-1",
                  "eventType": "auction.ended",
                  "eventVersion": 1,
                  "aggregateId": "auction-1",
                  "payload": {
                    "listingId": "listing-1",
                    "winnerId": "buyer-1",
                    "finalPrice": 14000
                  }
                }
                """);

        verify(listingService).markWon("listing-1", new BigDecimal("14000"));
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

        verify(listingService, times(1)).handleBidPlaced("listing-1", new BigDecimal("12500"));
    }
}
