package gg.statikk.notification.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdatePreferencesRequest(
        @NotNull Boolean emailEnabled,
        @Email @Size(max = 120) String email
) {}
