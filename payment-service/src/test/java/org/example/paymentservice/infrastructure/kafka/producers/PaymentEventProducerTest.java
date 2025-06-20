package org.example.paymentservice.infrastructure.kafka.producers;

import org.example.common.correlation.CorrelationConstants;
import org.example.common.events.PaymentEvent;
import org.example.common.headers.CustomHeaders;
import org.example.common.topics.KafkaTopics;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.slf4j.MDC;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentEventProducerTest {

    @Mock
    private KafkaTemplate<String, PaymentEvent> kafkaTemplate;

    @InjectMocks
    private PaymentEventProducer paymentEventProducer;

    private final UUID TEST_PAYMENT_ID = UUID.randomUUID();
    private final String TEST_CORRELATION_ID = "test-correlation-id";

    @Test
    void sendEvent_shouldSendMessageWithCorrectHeadersAndPayload() {
        PaymentEvent event = PaymentEvent.builder()
                .paymentId(TEST_PAYMENT_ID)
                .build();

        when(kafkaTemplate.send(any(Message.class))).thenReturn(CompletableFuture.completedFuture(null));
        MDC.put(CorrelationConstants.CORRELATION_ID_MDC, TEST_CORRELATION_ID);

        paymentEventProducer.sendEvent(event);

        verify(kafkaTemplate).send(argThat((Message<PaymentEvent> message) ->
                message.getHeaders().get(KafkaHeaders.TOPIC).equals(KafkaTopics.PAYMENT_EVENTS) &&
                        message.getHeaders().get(KafkaHeaders.KEY).equals(TEST_PAYMENT_ID.toString()) &&
                        message.getPayload().equals(event)
        ));
    }

    @Test
    void sendEvent_shouldHandleKafkaSendSuccess() {
        PaymentEvent event = PaymentEvent.builder()
                .paymentId(TEST_PAYMENT_ID)
                .build();

        when(kafkaTemplate.send(any(Message.class))).thenReturn(CompletableFuture.completedFuture(null));

        paymentEventProducer.sendEvent(event);

        verify(kafkaTemplate).send(any(Message.class));
    }

    @Test
    void sendEvent_shouldHandleKafkaSendFailure() {
        PaymentEvent event = PaymentEvent.builder()
                .paymentId(TEST_PAYMENT_ID)
                .build();

        CompletableFuture future = new CompletableFuture();
        future.completeExceptionally(new RuntimeException("Kafka error"));
        when(kafkaTemplate.send(any(Message.class))).thenReturn(future);

        paymentEventProducer.sendEvent(event);

        verify(kafkaTemplate).send(any(Message.class));
    }

    @Test
    void sendEvent_shouldUseCorrelationIdFromMDC() {
        PaymentEvent event = PaymentEvent.builder()
                .paymentId(TEST_PAYMENT_ID)
                .build();

        when(kafkaTemplate.send(any(Message.class))).thenReturn(CompletableFuture.completedFuture(null));
        MDC.put(CorrelationConstants.CORRELATION_ID_MDC, TEST_CORRELATION_ID);

        paymentEventProducer.sendEvent(event);

        verify(kafkaTemplate).send(argThat((Message<PaymentEvent> message) ->
                message.getHeaders().get(CustomHeaders.CORRELATION_ID_HEADER).equals(TEST_CORRELATION_ID)
        ));
    }
}