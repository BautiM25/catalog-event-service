package ar.edu.unnoba.pdyc2026.catalog_event_service.controller;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import ar.edu.unnoba.pdyc2026.catalog_event_service.dto.AddArtistRequest;
import ar.edu.unnoba.pdyc2026.catalog_event_service.dto.ArtistResponse;
import ar.edu.unnoba.pdyc2026.catalog_event_service.dto.CreateEventRequest;
import ar.edu.unnoba.pdyc2026.catalog_event_service.dto.EventDetail;
import ar.edu.unnoba.pdyc2026.catalog_event_service.dto.EventSummary;
import ar.edu.unnoba.pdyc2026.catalog_event_service.dto.RescheduleRequest;
import ar.edu.unnoba.pdyc2026.catalog_event_service.dto.UpdateEventRequest;
import ar.edu.unnoba.pdyc2026.catalog_event_service.model.Artist;
import ar.edu.unnoba.pdyc2026.catalog_event_service.model.Event;
import ar.edu.unnoba.pdyc2026.catalog_event_service.model.EventStatus;
import ar.edu.unnoba.pdyc2026.catalog_event_service.service.EventService;

@RestController
@RequestMapping("/admin")
public class EventController {

    @Autowired
    private EventService eventService;

    private ArtistResponse toArtistResponse(Artist artist) {
        return new ArtistResponse(artist.getId(), artist.getName(), artist.getGenre(), artist.isActive());
    }

    private EventSummary toSummary(Event event) {
        return new EventSummary(event.getId(), event.getName(), event.getStartDate(),
                event.getStatus(), event.getArtists().size());
    }

    private EventDetail toDetail(Event event) {
        List<ArtistResponse> artists = event.getArtists().stream()
                .map(this::toArtistResponse).collect(Collectors.toList());
        return new EventDetail(event.getId(), event.getName(), event.getDescription(),
                event.getStartDate(), event.getStatus(), artists);
    }

    @GetMapping("/events")
    public ResponseEntity<List<EventSummary>> getEvents(@RequestParam(value = "state", required = false) EventStatus state) {
        List<Event> events = eventService.findAll(Optional.ofNullable(state));
        List<EventSummary> response = events.stream().map(this::toSummary).collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/events/{id}")
    public ResponseEntity<EventDetail> getEvent(@PathVariable Long id) {
        Event event = eventService.findById(id)
                .orElseThrow(() -> new RuntimeException("Event not found"));
        return ResponseEntity.ok(toDetail(event));
    }

    @PostMapping("/events")
    public ResponseEntity<EventDetail> createEvent(@RequestBody CreateEventRequest request) {
        Event event = eventService.createEvent(request.name(), request.start_date(), request.description());
        return ResponseEntity.status(HttpStatus.CREATED).body(toDetail(event));
    }

    @PutMapping("/events/{id}")
    public ResponseEntity<EventDetail> updateEvent(@PathVariable Long id, @RequestBody UpdateEventRequest request) {
        Event event = eventService.updateEvent(id, request.name(), request.start_date(), request.description());
        return ResponseEntity.ok(toDetail(event));
    }

    @DeleteMapping("/events/{id}")
    public ResponseEntity<Void> deleteEvent(@PathVariable Long id) {
        eventService.deleteEvent(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/events/{id}/artists")
    public ResponseEntity<Void> addArtistToEvent(@PathVariable Long id, @RequestBody AddArtistRequest request) {
        eventService.addArtistToEvent(id, request.artist_id());
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @DeleteMapping("/events/{id}/artist/{artist_id}")
    public ResponseEntity<Void> removeArtistFromEvent(@PathVariable Long id, @PathVariable Long artist_id) {
        eventService.removeArtistFromEvent(id, artist_id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/events/{id}/confirmed")
    public ResponseEntity<Void> confirmEvent(@PathVariable Long id) {
        eventService.confirmEvent(id);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/events/{id}/rescheduled")
    public ResponseEntity<Void> rescheduleEvent(@PathVariable Long id, @RequestBody RescheduleRequest request) {
        eventService.rescheduleEvent(id, request.start_date());
        return ResponseEntity.ok().build();
    }

    @PutMapping("/events/{id}/canceled")
    public ResponseEntity<Void> cancelEvent(@PathVariable Long id) {
        eventService.cancelEvent(id);
        return ResponseEntity.ok().build();
    }
}