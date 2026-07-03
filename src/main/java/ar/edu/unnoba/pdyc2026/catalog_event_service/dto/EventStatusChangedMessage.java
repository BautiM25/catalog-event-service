package ar.edu.unnoba.pdyc2026.catalog_event_service.dto;

import java.util.List;

import ar.edu.unnoba.pdyc2026.catalog_event_service.model.EventStatus;

public record EventStatusChangedMessage(
    Long eventId,
    String eventName,
    List<Long> artistIds,
    EventStatus previousState,
    EventStatus currentState
) {}