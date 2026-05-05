package id.ac.ui.cs.advprog.bidmartcatalogueservice.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CatalogueRabbitConfig {

    @Bean
    TopicExchange bidmartEventsExchange(
            @Value("${bidmart.rabbitmq.events-exchange:bidmart.events}") String exchangeName
    ) {
        return new TopicExchange(exchangeName, true, false);
    }

    @Bean
    Queue catalogueAuctionEventsQueue(
            @Value("${bidmart.rabbitmq.catalogue.auction-events-queue:catalogue.auction-events}") String queueName
    ) {
        return new Queue(queueName, true);
    }

    @Bean
    Binding catalogueBidPlacedBinding(Queue catalogueAuctionEventsQueue, TopicExchange bidmartEventsExchange) {
        return BindingBuilder.bind(catalogueAuctionEventsQueue)
                .to(bidmartEventsExchange)
                .with("auction.bid-placed.v1");
    }

    @Bean
    Binding catalogueAuctionEndedBinding(Queue catalogueAuctionEventsQueue, TopicExchange bidmartEventsExchange) {
        return BindingBuilder.bind(catalogueAuctionEventsQueue)
                .to(bidmartEventsExchange)
                .with("auction.ended.v1");
    }
}
