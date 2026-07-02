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

import ar.edu.unnoba.pdyc2026.catalog_event_service.dto.ArtistResponse;
import ar.edu.unnoba.pdyc2026.catalog_event_service.dto.CreateArtistRequest;
import ar.edu.unnoba.pdyc2026.catalog_event_service.dto.UpdateArtistRequest;
import ar.edu.unnoba.pdyc2026.catalog_event_service.model.Artist;
import ar.edu.unnoba.pdyc2026.catalog_event_service.model.Genre;
import ar.edu.unnoba.pdyc2026.catalog_event_service.service.ArtistService;

@RestController
@RequestMapping("/admin")
public class ArtistController {

    @Autowired
    private ArtistService artistService;

    private ArtistResponse toResponse(Artist artist) {
        return new ArtistResponse(artist.getId(), artist.getName(), artist.getGenre(), artist.isActive());
    }

    @GetMapping("/artists")
    public ResponseEntity<List<ArtistResponse>> getArtists(@RequestParam(value = "genre", required = false) Genre genre) {
        List<Artist> artists = artistService.findAll(Optional.ofNullable(genre));
        List<ArtistResponse> response = artists.stream().map(this::toResponse).collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/artists/{id}")
    public ResponseEntity<ArtistResponse> getArtist(@PathVariable Long id) {
        Artist artist = artistService.findById(id)
                .orElseThrow(() -> new RuntimeException("Artist not found"));
        return ResponseEntity.ok(toResponse(artist));
    }

    @PostMapping("/artists")
    public ResponseEntity<ArtistResponse> createArtist(@RequestBody CreateArtistRequest request) {
        Artist artist = artistService.createArtist(request.name(), request.genre());
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(artist));
    }

    @PutMapping("/artists/{id}")
    public ResponseEntity<ArtistResponse> updateArtist(@PathVariable Long id, @RequestBody UpdateArtistRequest request) {
        Artist artist = artistService.updateArtist(id, request.name(), request.genre());
        return ResponseEntity.ok(toResponse(artist));
    }

    @DeleteMapping("/artists/{id}")
    public ResponseEntity<Void> deleteArtist(@PathVariable Long id) {
        artistService.deleteOrDeactivateArtist(id);
        return ResponseEntity.noContent().build();
    }
}