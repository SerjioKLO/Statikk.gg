package gg.statikk.notification.service;

import gg.statikk.notification.domain.NotificationPreference;
import gg.statikk.notification.dto.PreferencesResponse;
import gg.statikk.notification.dto.UpdatePreferencesRequest;
import gg.statikk.notification.exception.InvalidPreferencesException;
import gg.statikk.notification.repository.NotificationPreferenceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PreferenceService {

    private final NotificationPreferenceRepository preferenceRepository;

    public PreferenceService(NotificationPreferenceRepository preferenceRepository) {
        this.preferenceRepository = preferenceRepository;
    }

    /** Sin fila guardada = valores por defecto: correo desactivado (opt-in). */
    @Transactional(readOnly = true)
    public PreferencesResponse get(Long userId) {
        return preferenceRepository.findById(userId)
                .map(p -> new PreferencesResponse(p.isEmailEnabled(), p.getEmail()))
                .orElse(new PreferencesResponse(false, null));
    }

    @Transactional
    public PreferencesResponse update(Long userId, UpdatePreferencesRequest request) {
        String email = request.email() == null ? null : request.email().trim();
        if (email != null && email.isEmpty()) {
            email = null;
        }
        boolean enabled = Boolean.TRUE.equals(request.emailEnabled());
        if (enabled && email == null) {
            throw new InvalidPreferencesException("Para activar el correo debes indicar un email");
        }

        NotificationPreference preference = preferenceRepository.findById(userId)
                .orElseGet(() -> new NotificationPreference(userId));
        preference.update(enabled, email);
        preferenceRepository.save(preference);

        return new PreferencesResponse(enabled, email);
    }
}
