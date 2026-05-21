package id.ac.ui.cs.advprog.bidmartcatalogueservice.event;

import id.ac.ui.cs.advprog.bidmartcatalogueservice.dto.ListingClosedByAdminEvent;
import id.ac.ui.cs.advprog.bidmartcatalogueservice.dto.ListingCreatedEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
public class ListingEventPublisher {

    private final ApplicationEventPublisher applicationEventPublisher;

    public ListingEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }

    public void publishListingCreated(ListingCreatedEvent event) {
        applicationEventPublisher.publishEvent(event);
    }

    public void publishListingClosedByAdmin(ListingClosedByAdminEvent event) {
        applicationEventPublisher.publishEvent(event);
    }
}
