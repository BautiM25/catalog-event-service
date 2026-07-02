package ar.edu.unnoba.pdyc2026.catalog_event_service.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ar.edu.unnoba.pdyc2026.catalog_event_service.dto.EventDetail;
import ar.edu.unnoba.pdyc2026.catalog_event_service.dto.EventSummary;
import ar.edu.unnoba.pdyc2026.catalog_event_service.service.EventService;

@RestController
@RequestMapping("/events")
public class PublicEventController {

    @Autowired
    private EventService eventService;

    // GET /events
    @GetMapping
    public ResponseEntity<List<EventSummary>> getPublicEvents() {
        return ResponseEntity.ok(eventService.findPublicEvents());
    }

    // GET /events/{eventId}
    @GetMapping("/{eventId}")
    public ResponseEntity<EventDetail> getPublicEventDetail(@PathVariable Long eventId) {
        try {
            EventDetail detail = eventService.findPublicEventDetail(eventId);
            return ResponseEntity.ok(detail);
        } catch (RuntimeException e) {
            // Si el evento es 'tentative' u ocurrió un error, devolvemos un 403 Forbidden para ocultar la info
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }
}