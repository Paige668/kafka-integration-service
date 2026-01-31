package com.example.eventprocessor.repository;

import com.example.eventprocessor.model.ProcessedEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ProcessedEventRepository extends JpaRepository<ProcessedEvent, Long> {
    Optional<ProcessedEvent> findByEventId(String eventId);
}

