package id.ac.ui.cs.advprog.bidmartcatalogueservice.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

@Component
public class BidmartCatalogueMetrics {

    private final Counter listingsCreatedTotal;
    private final Counter listingsPublishedTotal;

    public BidmartCatalogueMetrics(MeterRegistry registry) {
        listingsCreatedTotal = Counter.builder("bidmart_catalogue_listings_created_total")
                .description("Catalogue listings created")
                .register(registry);
        listingsPublishedTotal = Counter.builder("bidmart_catalogue_listings_published_total")
                .description("Catalogue listings published")
                .register(registry);
    }

    public void recordListingCreated() {
        listingsCreatedTotal.increment();
    }

    public void recordListingPublished() {
        listingsPublishedTotal.increment();
    }
}
