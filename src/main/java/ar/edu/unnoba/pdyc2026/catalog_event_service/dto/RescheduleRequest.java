package ar.edu.unnoba.pdyc2026.catalog_event_service.dto;

import java.time.LocalDate;

public record RescheduleRequest(LocalDate start_date) {}