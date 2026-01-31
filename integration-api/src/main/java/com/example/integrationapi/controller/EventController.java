package com.example.integrationapi.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.example.integrationapi.model.Event;
import com.fasterxml.jackson.core.JsonProcessingException;

@RestController
public class EventController {

    private static final Logger log = LoggerFactory.getLogger(EventController.class);

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final com.fasterxml.jackson.databind.ObjectMapper objectMapper;

    public EventController(KafkaTemplate<String, String> kafkaTemplate, com.fasterxml.jackson.databind.ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    @PostMapping("/events")
    public ResponseEntity<String> publishEvent(@RequestBody Event event) {
        log.info("Received event: {}", event);
        
        // Basic validation
        if (event.getEventId() == null) {
            return ResponseEntity.badRequest().body("eventId is required");
        }
        // Note: 'type' validation is intentionally omitted here to demonstrate 
        // Consumer-side validation and DLQ routing.

        try {
            String json = objectMapper.writeValueAsString(event);
            kafkaTemplate.send("events.v1", event.getEventId(), json);
            return ResponseEntity.ok("Event published");
        } catch (JsonProcessingException e) {
            log.error("Failed to publish event", e);
            return ResponseEntity.internalServerError().body("Failed to publish event");
        }
    }
}
