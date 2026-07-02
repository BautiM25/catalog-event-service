package ar.edu.unnoba.pdyc2026.catalog_event_service.dto;

import ar.edu.unnoba.pdyc2026.catalog_event_service.model.Genre;

public record ArtistResponse(Long id, String name, Genre genre, boolean active) {}