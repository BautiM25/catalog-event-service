package ar.edu.unnoba.pdyc2026.catalog_event_service.dto;

import ar.edu.unnoba.pdyc2026.catalog_event_service.model.Genre;

public record UpdateArtistRequest(String name, Genre genre) {}