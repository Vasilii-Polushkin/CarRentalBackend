package org.example.paymentservice.infrastructure.kafka.producers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.common.correlation.CorrelationConstants;
import org.example.common.events.PaymentEvent;
import org.example.common.headers.CustomHeaders;
import org.example.common.topics.KafkaTopics;
import org.slf4j.MDC;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentEventProducer {

    private final KafkaTemplate<String, PaymentEvent> kafkaTemplate;

    public void sendEvent(PaymentEvent event) {
        try {
            Message<PaymentEvent> message = MessageBuilder
                    .withPayload(event)
                    .setHeader(KafkaHeaders.TOPIC, KafkaTopics.PAYMENT_EVENTS)
                    .setHeader(KafkaHeaders.KEY, event.getPaymentId().toString())
                    .setHeader(CustomHeaders.CORRELATION_ID_HEADER, MDC.get(CorrelationConstants.CORRELATION_ID_MDC))
                    .build();

            kafkaTemplate.send(message)
                    .whenComplete((result, ex) -> {
                        if (ex == null) {
                            log.info("Sent payment event: {}", event);
                        } else {
                            log.error("Failed to send payment event: {}", event, ex);
                        }
                    });
        } catch (Exception e) {
            log.error("Error sending payment event: {}", event, e);
        }
    }
}
