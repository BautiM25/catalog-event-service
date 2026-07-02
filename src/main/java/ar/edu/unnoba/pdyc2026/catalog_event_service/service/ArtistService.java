package ar.edu.unnoba.pdyc2026.catalog_event_service.service;


import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ar.edu.unnoba.pdyc2026.catalog_event_service.model.Artist;
import ar.edu.unnoba.pdyc2026.catalog_event_service.model.Genre;
import ar.edu.unnoba.pdyc2026.catalog_event_service.repository.ArtistRepository;

@Service
public class ArtistService {

    @Autowired
    private ArtistRepository artistRepository;

    public List<Artist> findAll(Optional<Genre> genre) {
        return genre.map(artistRepository::findByGenre)
                    .orElseGet(artistRepository::findAll);
    }

    public Optional<Artist> findById(Long id) {
        return artistRepository.findById(id);
    }

    @Transactional
    public Artist createArtist(String name, Genre genre) {
        Artist artist = new Artist();
        artist.setName(name);
        artist.setGenre(genre);
        artist.setActive(true);
        return artistRepository.save(artist);
    }

    @Transactional
    public Artist updateArtist(Long id, String name, Genre genre) {
        Artist artist = artistRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Artist not found"));
        // Cannot edit if already participated in any event
        if (!artist.getEvents().isEmpty()) {
            throw new IllegalStateException("Cannot edit artist that has participated in events");
        }
        artist.setName(name);
        artist.setGenre(genre);
        return artistRepository.save(artist);
    }

    @Transactional
    public void deleteOrDeactivateArtist(Long id) {
        Artist artist = artistRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Artist not found"));
        if (artist.getEvents().isEmpty()) {
            artistRepository.delete(artist);
        } else {
            artist.setActive(false);
            artistRepository.save(artist);
        }
    }

    public List<ar.edu.unnoba.pdyc2026.catalog_event_service.dto.ArtistResponse> findActiveArtists() {
    return artistRepository.findByActiveTrue().stream()
            .map(artist -> new ar.edu.unnoba.pdyc2026.catalog_event_service.dto.ArtistResponse(
                    artist.getId(),
                    artist.getName(),
                    artist.getGenre(),
                    artist.isActive()
            ))
            .collect(java.util.stream.Collectors.toList());
    }
}