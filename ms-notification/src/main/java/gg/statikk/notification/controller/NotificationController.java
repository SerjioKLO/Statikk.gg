package gg.statikk.notification.controller;

import gg.statikk.notification.dto.MarkAllReadResponse;
import gg.statikk.notification.dto.NotificationResponse;
import gg.statikk.notification.dto.PreferencesResponse;
import gg.statikk.notification.dto.UnreadCountResponse;
import gg.statikk.notification.dto.UpdatePreferencesRequest;
import gg.statikk.notification.service.NotificationQueryService;
import gg.statikk.notification.service.PreferenceService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** Todo es "mis" notificaciones: el userId sale del header X-User-Id que pone el gateway, nunca de la URL. */
@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationQueryService queryService;
    private final PreferenceService preferenceService;

    public NotificationController(NotificationQueryService queryService, PreferenceService preferenceService) {
        this.queryService = queryService;
        this.preferenceService = preferenceService;
    }

    @GetMapping
    public ResponseEntity<List<NotificationResponse>> list(
            @RequestHeader("X-User-Id") Long userId,
            @RequestParam(defaultValue = "false") boolean unreadOnly,
            @RequestParam(defaultValue = "20") int limit) {
        return ResponseEntity.ok(queryService.list(userId, unreadOnly, limit));
    }

    @GetMapping("/unread-count")
    public ResponseEntity<UnreadCountResponse> unreadCount(@RequestHeader("X-User-Id") Long userId) {
        return ResponseEntity.ok(queryService.unreadCount(userId));
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<NotificationResponse> markRead(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long id) {
        return ResponseEntity.ok(queryService.markRead(userId, id));
    }

    @PostMapping("/read-all")
    public ResponseEntity<MarkAllReadResponse> markAllRead(@RequestHeader("X-User-Id") Long userId) {
        return ResponseEntity.ok(queryService.markAllRead(userId));
    }

    @GetMapping("/preferences")
    public ResponseEntity<PreferencesResponse> getPreferences(@RequestHeader("X-User-Id") Long userId) {
        return ResponseEntity.ok(preferenceService.get(userId));
    }

    @PutMapping("/preferences")
    public ResponseEntity<PreferencesResponse> updatePreferences(
            @RequestHeader("X-User-Id") Long userId,
            @Valid @RequestBody UpdatePreferencesRequest request) {
        return ResponseEntity.ok(preferenceService.update(userId, request));
    }
}
