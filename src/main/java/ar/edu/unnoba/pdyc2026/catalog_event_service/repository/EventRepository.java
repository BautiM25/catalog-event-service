package ar.edu.unnoba.pdyc2026.catalog_event_service.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import ar.edu.unnoba.pdyc2026.catalog_event_service.model.Event;
import ar.edu.unnoba.pdyc2026.catalog_event_service.model.EventStatus;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {
    List<Event> findByStatus(EventStatus status);
    List<Event> findByStatusIn(List<EventStatus> statuses);
}