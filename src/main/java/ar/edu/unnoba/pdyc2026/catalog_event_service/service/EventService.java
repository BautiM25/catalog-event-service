package ar.edu.unnoba.pdyc2026.catalog_event_service.service;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ar.edu.unnoba.pdyc2026.catalog_event_service.dto.ArtistResponse;
import ar.edu.unnoba.pdyc2026.catalog_event_service.dto.EventDetail;
import ar.edu.unnoba.pdyc2026.catalog_event_service.dto.EventStatusChangedMessage;
import ar.edu.unnoba.pdyc2026.catalog_event_service.dto.EventSummary;
import ar.edu.unnoba.pdyc2026.catalog_event_service.model.Artist;
import ar.edu.unnoba.pdyc2026.catalog_event_service.model.Event;
import ar.edu.unnoba.pdyc2026.catalog_event_service.model.EventStatus;
import ar.edu.unnoba.pdyc2026.catalog_event_service.repository.ArtistRepository;
import ar.edu.unnoba.pdyc2026.catalog_event_service.repository.EventRepository;

@Service
public class EventService {

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private ArtistRepository artistRepository;

    @Autowired
    private org.springframework.amqp.rabbit.core.RabbitTemplate rabbitTemplate;

    public List<Event> findAll(Optional<EventStatus> state) {
        return state.map(eventRepository::findByStatus)
                    .orElseGet(eventRepository::findAll);
    }

    public Optional<Event> findById(Long id) {
        return eventRepository.findById(id);
    }

    @Transactional
    public Event createEvent(String name, LocalDate startDate, String description) {
        Event event = new Event();
        event.setName(name);
        event.setStartDate(startDate);
        event.setDescription(description);
        event.setStatus(EventStatus.tentative);
        return eventRepository.save(event);
    }

    @Transactional
    public Event updateEvent(Long id, String name, LocalDate startDate, String description) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Event not found"));
        if (event.getStatus() != EventStatus.tentative) {
            throw new IllegalStateException("Only tentative events can be updated");
        }
        event.setName(name);
        event.setStartDate(startDate);
        event.setDescription(description);
        return eventRepository.save(event);
    }

    @Transactional
    public void deleteEvent(Long id) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Event not found"));
        if (event.getStatus() != EventStatus.tentative) {
            throw new IllegalStateException("Only tentative events can be deleted");
        }
        eventRepository.delete(event);
    }

    @Transactional
    public void addArtistToEvent(Long eventId, Long artistId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found"));
        Artist artist = artistRepository.findById(artistId)
                .orElseThrow(() -> new RuntimeException("Artist not found"));

        if (event.getStatus() != EventStatus.tentative) {
            throw new IllegalStateException("Can only add artists to tentative events");
        }
        if (!artist.isActive()) {
            throw new IllegalStateException("Artist is deactivated and cannot be added to events");
        }
        if (event.getArtists().contains(artist)) {
            throw new IllegalStateException("Artist already in event");
        }
        event.getArtists().add(artist);
        eventRepository.save(event);
    }

    @Transactional
    public void removeArtistFromEvent(Long eventId, Long artistId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found"));
        Artist artist = artistRepository.findById(artistId)
                .orElseThrow(() -> new RuntimeException("Artist not found"));

        if (event.getStatus() != EventStatus.tentative) {
            throw new IllegalStateException("Can only remove artists from tentative events");
        }
        if (!event.getArtists().contains(artist)) {
            throw new IllegalStateException("Artist not in event");
        }
        event.getArtists().remove(artist);
        eventRepository.save(event);
    }

    @Transactional
    public void confirmEvent(Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found"));
        if (event.getStatus() != EventStatus.tentative) {
            throw new IllegalStateException("Only tentative events can be confirmed");
        }
        if (!event.getStartDate().isAfter(LocalDate.now())) {
            throw new IllegalStateException("Cannot confirm event with past or today's date");
        }

        EventStatus previousState = event.getStatus(); // Guardamos el estado previo (tentative)
        event.setStatus(EventStatus.confirmed);
        Event savedEvent = eventRepository.save(event);

        List<Long> artistIds = savedEvent.getArtists().stream().map(Artist::getId).collect(Collectors.toList());
        EventStatusChangedMessage message = new EventStatusChangedMessage(
            savedEvent.getId(), savedEvent.getName(), artistIds, previousState, EventStatus.confirmed
        );

        // Enviamos el mensaje al Exchange de RabbitMQ
        rabbitTemplate.convertAndSend("event.exchange", "event.status.changed", message);
    }

    @Transactional
    public void rescheduleEvent(Long eventId, LocalDate newStartDate) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found"));
        if (event.getStatus() != EventStatus.confirmed && event.getStatus() != EventStatus.rescheduled) {
            throw new IllegalStateException("Only confirmed or rescheduled events can be rescheduled");
        }
        if (!event.getStartDate().isAfter(LocalDate.now()) && !event.getStartDate().isEqual(LocalDate.now())) {
            throw new IllegalStateException("Only events with future or today's date can be rescheduled");
        }
        if (!newStartDate.isAfter(LocalDate.now())) {
            throw new IllegalStateException("New date must be in the future");
        }

        EventStatus previousState = event.getStatus(); // Guardamos el estado previo
        event.setStartDate(newStartDate);
        event.setStatus(EventStatus.rescheduled);
        Event savedEvent = eventRepository.save(event);

        List<Long> artistIds = savedEvent.getArtists().stream().map(Artist::getId).collect(Collectors.toList());
        EventStatusChangedMessage message = new EventStatusChangedMessage(
            savedEvent.getId(), savedEvent.getName(), artistIds, previousState, EventStatus.rescheduled
        );

        // Enviamos el mensaje al Exchange de RabbitMQ
        rabbitTemplate.convertAndSend("event.exchange", "event.status.changed", message);
    }

    @Transactional
    public void cancelEvent(Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found"));
        if (event.getStatus() != EventStatus.confirmed && event.getStatus() != EventStatus.rescheduled) {
            throw new IllegalStateException("Only confirmed or rescheduled events can be cancelled");
        }

        EventStatus previousState = event.getStatus(); // Guardamos el estado previo
        event.setStatus(EventStatus.cancelled);
        Event savedEvent = eventRepository.save(event);

        List<Long> artistIds = savedEvent.getArtists().stream().map(Artist::getId).collect(Collectors.toList());
        EventStatusChangedMessage message = new EventStatusChangedMessage(
            savedEvent.getId(), savedEvent.getName(), artistIds, previousState, EventStatus.cancelled
        );

        // Enviamos el mensaje al Exchange de RabbitMQ
        rabbitTemplate.convertAndSend("event.exchange", "event.status.changed", message);
    }

    // Para: GET /events
    public List<EventSummary> findPublicEvents() {
        LocalDate today = LocalDate.now();
        // Solo se permite mostrar confirmados o reprogramados públicamente
        List<EventStatus> allowedStatuses = Arrays.asList(EventStatus.confirmed, EventStatus.rescheduled);

        return eventRepository.findByStatusIn(allowedStatuses).stream()
                .filter(event -> !event.getStartDate().isBefore(today)) // Vigentes y próximos
                .map(event -> new EventSummary(
                        event.getId(),
                        event.getName(),
                        event.getStartDate(),
                        event.getStatus(),
                        event.getArtists().size()
                ))
                .collect(Collectors.toList());
    }

    // Para: GET /artists/{artistId}/events
    public List<EventSummary> findPublicEventsByArtist(Long artistId) {
        Artist artist = artistRepository.findByIdAndActiveTrue(artistId)
                .orElseThrow(() -> new RuntimeException("Active artist not found"));

        LocalDate today = LocalDate.now();
        List<EventStatus> allowedStatuses = Arrays.asList(EventStatus.confirmed, EventStatus.rescheduled);

        return artist.getEvents().stream()
                .filter(event -> allowedStatuses.contains(event.getStatus()))
                .filter(event -> !event.getStartDate().isBefore(today))
                .map(event -> new EventSummary(
                        event.getId(),
                        event.getName(),
                        event.getStartDate(),
                        event.getStatus(),
                        event.getArtists().size()
                ))
                .collect(Collectors.toList());
    }

    // Para: GET /events/{eventId}
    public EventDetail findPublicEventDetail(Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found"));

        // REGLA: NO se puede acceder a información de eventos en estado "Tentative"
        if (event.getStatus() == EventStatus.tentative) {
            throw new RuntimeException("Access Denied: Event is tentative");
        }

        List<ArtistResponse> artists = event.getArtists().stream()
                .map(artist -> new ArtistResponse(
                        artist.getId(),
                        artist.getName(),
                        artist.getGenre(),
                        artist.isActive()
                ))
                .collect(Collectors.toList());

        return new EventDetail(
                event.getId(),
                event.getName(),
                event.getDescription(),
                event.getStartDate(),
                event.getStatus(),
                artists
        );
    }
}