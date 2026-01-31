package com.example.integrationapi.model;

import java.util.Map;

public class Event {
    private String eventId;
    private String type;
    private Map<String, Object> payload;

    public Event() {
    }

    public Event(String eventId, String type, Map<String, Object> payload) {
        this.eventId = eventId;
        this.type = type;
        this.payload = payload;
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

    public Map<String, Object> getPayload() {
        return payload;
    }

    public void setPayload(Map<String, Object> payload) {
        this.payload = payload;
    }

    @Override
    public String toString() {
        return "Event{" +
                "eventId='" + eventId + '\'' +
                ", type='" + type + '\'' +
                ", payload=" + payload +
                '}';
    }
}
