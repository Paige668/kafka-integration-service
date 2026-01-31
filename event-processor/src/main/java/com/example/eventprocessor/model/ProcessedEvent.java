package com.example.eventprocessor.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "processed_events")
public class ProcessedEvent {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String eventId;

    private String type;

    @Column(columnDefinition = "TEXT")
    private String payload;

    private LocalDateTime processedAt;

    public ProcessedEvent() {
    }

    public ProcessedEvent(Long id, String eventId, String type, String payload, LocalDateTime processedAt) {
        this.id = id;
        this.eventId = eventId;
        this.type = type;
        this.payload = payload;
        this.processedAt = processedAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    public LocalDateTime getProcessedAt() {
        return processedAt;
    }

    public void setProcessedAt(LocalDateTime processedAt) {
        this.processedAt = processedAt;
    }

    @Override
    public String toString() {
        return "ProcessedEvent{" +
                "id=" + id +
                ", eventId='" + eventId + '\'' +
                ", type='" + type + '\'' +
                ", payload='" + payload + '\'' +
                ", processedAt=" + processedAt +
                '}';
    }
}
