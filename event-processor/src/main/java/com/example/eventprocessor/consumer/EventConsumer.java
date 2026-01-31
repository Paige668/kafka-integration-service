package com.example.eventprocessor.consumer;

import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.example.eventprocessor.model.ProcessedEvent;
import com.example.eventprocessor.repository.ProcessedEventRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class EventConsumer {

    private static final Logger log = LoggerFactory.getLogger(EventConsumer.class);

    private final ProcessedEventRepository repository;
    private final ObjectMapper objectMapper;
    private final KafkaTemplate<String, String> kafkaTemplate;

    public EventConsumer(ProcessedEventRepository repository, ObjectMapper objectMapper, KafkaTemplate<String, String> kafkaTemplate) {
        this.repository = repository;
        this.objectMapper = objectMapper;
        this.kafkaTemplate = kafkaTemplate;
    }

    @KafkaListener(topics = "events.v1", groupId = "event-processor-group")
    public void consume(String message) {
        log.info("Consumed message: {}", message);

        try {
            JsonNode root = objectMapper.readTree(message);
            
            // Validation
            if (!root.has("eventId") || !root.has("type")) {
                throw new IllegalArgumentException("Missing required fields: eventId or type");
            }

            String eventId = root.get("eventId").asText();
            String type = root.get("type").asText();

            // Idempotency Check (Optimization before DB hit, though DB constraint is final guard)
            if (repository.findByEventId(eventId).isPresent()) {
                log.info("Event {} already processed. Skipping.", eventId);
                return;
            }

            ProcessedEvent event = new ProcessedEvent();
            event.setEventId(eventId);
            event.setType(type);
            event.setPayload(message);
            event.setProcessedAt(LocalDateTime.now());

            try {
                repository.save(event);
                log.info("Successfully processed event: {}", eventId);
            } catch (DataIntegrityViolationException e) {
                log.info("Event {} duplicate detected during save. Skipping.", eventId);
            }

        } catch (JsonProcessingException | IllegalArgumentException e) {
            log.error("Error processing message. Sending to DLQ.", e);
            sendToDlq(message, e.getMessage());
        }
    }

    private void sendToDlq(String originalMessage, String errorReason) {
        try {
            // Enrich with error if possible, or just send original with header? 
            // Simple approach: Send JSON with error metadata
            var errorNode = objectMapper.createObjectNode();
            errorNode.put("originalMessage", originalMessage);
            errorNode.put("error", errorReason);
            errorNode.put("timestamp", LocalDateTime.now().toString());
            
            kafkaTemplate.send("events.dlq", errorNode.toString());
        } catch (Exception e) {
            log.error("Failed to send to DLQ!", e);
        }
    }
}

