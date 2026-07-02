package ar.edu.unnoba.pdyc2026.catalog_event_service.dto;

import java.time.LocalDate;

import ar.edu.unnoba.pdyc2026.catalog_event_service.model.EventStatus;

public record EventSummary(Long id, String name, LocalDate startDate, EventStatus status, int artistCount) {}