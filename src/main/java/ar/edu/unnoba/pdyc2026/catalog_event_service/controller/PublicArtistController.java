package ar.edu.unnoba.pdyc2026.catalog_event_service.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ar.edu.unnoba.pdyc2026.catalog_event_service.dto.ArtistResponse;
import ar.edu.unnoba.pdyc2026.catalog_event_service.dto.EventSummary;
import ar.edu.unnoba.pdyc2026.catalog_event_service.service.ArtistService;
import ar.edu.unnoba.pdyc2026.catalog_event_service.service.EventService;

@RestController
@RequestMapping("/artists")
public class PublicArtistController {

    @Autowired
    private ArtistService artistService;

    @Autowired
    private EventService eventService;

    // GET /artists
    @GetMapping
    public ResponseEntity<List<ArtistResponse>> getActiveArtists() {
        return ResponseEntity.ok(artistService.findActiveArtists());
    }

    // GET /artists/{artistId}/events
    @GetMapping("/{artistId}/events")
    public ResponseEntity<List<EventSummary>> getArtistPublicEvents(@PathVariable Long artistId) {
        return ResponseEntity.ok(eventService.findPublicEventsByArtist(artistId));
    }
}