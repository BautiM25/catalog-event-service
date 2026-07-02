package ar.edu.unnoba.pdyc2026.catalog_event_service.dto;

import java.time.LocalDate;
import java.util.List;

import ar.edu.unnoba.pdyc2026.catalog_event_service.model.EventStatus;

public record EventDetail(Long id, String name, String description, LocalDate startDate,
                          EventStatus status, List<ArtistResponse> artists) {}