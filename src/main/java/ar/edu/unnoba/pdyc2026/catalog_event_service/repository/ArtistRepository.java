package ar.edu.unnoba.pdyc2026.catalog_event_service.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import ar.edu.unnoba.pdyc2026.catalog_event_service.model.Artist;
import ar.edu.unnoba.pdyc2026.catalog_event_service.model.Genre;

@Repository
public interface ArtistRepository extends JpaRepository<Artist, Long> {
    List<Artist> findByGenre(Genre genre);
    Optional<Artist> findByIdAndActiveTrue(Long id);
    List<Artist> findByActiveTrue();
}