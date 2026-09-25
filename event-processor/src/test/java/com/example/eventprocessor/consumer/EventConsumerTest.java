package com.example.eventprocessor.consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.kafka.core.KafkaTemplate;

import com.example.eventprocessor.model.ProcessedEvent;
import com.example.eventprocessor.repository.ProcessedEventRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

class EventConsumerTest {

    private ProcessedEventRepository repository;
    private KafkaTemplate<String, String> kafkaTemplate;
    private EventConsumer consumer;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        repository = mock(ProcessedEventRepository.class);
        kafkaTemplate = mock(KafkaTemplate.class);
        consumer = new EventConsumer(repository, new ObjectMapper(), kafkaTemplate);
    }

    @Test
    void validEventIsSaved() {
        when(repository.findByEventId("evt-001")).thenReturn(Optional.empty());

        consumer.consume("{\"eventId\":\"evt-001\",\"type\":\"ORDER_CREATED\",\"payload\":{\"amount\":100}}");

        ArgumentCaptor<ProcessedEvent> saved = ArgumentCaptor.forClass(ProcessedEvent.class);
        verify(repository).save(saved.capture());
        assertThat(saved.getValue().getEventId()).isEqualTo("evt-001");
        assertThat(saved.getValue().getType()).isEqualTo("ORDER_CREATED");
        verify(kafkaTemplate, never()).send(anyString(), anyString());
    }

    @Test
    void duplicateEventIsSkipped() {
        when(repository.findByEventId("evt-001")).thenReturn(Optional.of(new ProcessedEvent()));

        consumer.consume("{\"eventId\":\"evt-001\",\"type\":\"ORDER_CREATED\"}");

        verify(repository, never()).save(any());
        verify(kafkaTemplate, never()).send(anyString(), anyString());
    }

    @Test
    void nullTypeFromApiGoesToDlq() {
        // This is what the integration API publishes when the client omits "type"
        consumer.consume("{\"eventId\":\"evt-error-1\",\"type\":null,\"payload\":{\"reason\":\"missing type\"}}");

        verify(repository, never()).save(any());
        verify(kafkaTemplate).send(eq("events.dlq"), anyString());
    }

    @Test
    void absentTypeGoesToDlq() {
        consumer.consume("{\"eventId\":\"evt-error-2\"}");

        verify(repository, never()).save(any());
        verify(kafkaTemplate).send(eq("events.dlq"), anyString());
    }

    @Test
    void blankTypeGoesToDlq() {
        consumer.consume("{\"eventId\":\"evt-error-3\",\"type\":\"  \"}");

        verify(repository, never()).save(any());
        verify(kafkaTemplate).send(eq("events.dlq"), anyString());
    }

    @Test
    void malformedJsonGoesToDlq() {
        consumer.consume("not json");

        verify(repository, never()).save(any());
        verify(kafkaTemplate).send(eq("events.dlq"), anyString());
    }
}
