package id.ac.ui.cs.advprog.bidmartcatalogueservice.event;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import id.ac.ui.cs.advprog.bidmartcatalogueservice.service.ListingService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class AuctionCatalogueEventConsumer {

    private static final String BID_PLACED_V1 = "auction.bid-placed.v1";
    private static final String AUCTION_ENDED_V1 = "auction.ended.v1";

    private final ObjectMapper objectMapper;
    private final ListingService listingService;
    private final Set<String> processedEventIds = ConcurrentHashMap.newKeySet();

    public AuctionCatalogueEventConsumer(ObjectMapper objectMapper, ListingService listingService) {
        this.objectMapper = objectMapper;
        this.listingService = listingService;
    }

    @RabbitListener(queues = "${bidmart.rabbitmq.catalogue.auction-events-queue:catalogue.auction-events}")
    public void consume(String message) throws JsonProcessingException {
        JsonNode envelope = objectMapper.readTree(message);
        String eventId = envelope.path("eventId").asText("");
        if (!eventId.isBlank() && !processedEventIds.add(eventId)) {
            return;
        }

        String eventType = normalizeEventType(envelope);
        JsonNode payload = envelope.path("payload");
        if (BID_PLACED_V1.equals(eventType)) {
            handleBidPlaced(payload);
            return;
        }
        if (AUCTION_ENDED_V1.equals(eventType)) {
            handleAuctionEnded(payload);
        }
    }

    private String normalizeEventType(JsonNode envelope) {
        String eventType = envelope.path("eventType").asText("");
        int eventVersion = envelope.path("eventVersion").asInt(1);
        if (eventVersion == 1 && !eventType.endsWith(".v1")) {
            return eventType + ".v1";
        }
        return eventType;
    }

    private void handleBidPlaced(JsonNode payload) {
        String listingId = payload.path("listingId").asText("");
        if (listingId.isBlank()) {
            return;
        }
        listingService.handleBidPlaced(listingId, decimal(payload.path("currentPrice")));
    }

    private void handleAuctionEnded(JsonNode payload) {
        String listingId = payload.path("listingId").asText("");
        if (listingId.isBlank()) {
            return;
        }
        String winnerId = payload.path("winnerId").asText("");
        if (winnerId.isBlank()) {
            listingService.markUnsold(listingId);
            return;
        }
        listingService.markWon(listingId, decimal(payload.path("finalPrice")));
    }

    private BigDecimal decimal(JsonNode node) {
        if (node.isNumber()) {
            return node.decimalValue();
        }
        return new BigDecimal(node.asText("0"));
    }
}
