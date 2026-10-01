package gg.statikk.auth.dto;

public record AuthResponse(
        String token,
        String tokenType,
        long expiresInMinutes
) {
    public static AuthResponse of(String token, long expiresInMinutes) {
        return new AuthResponse(token, "Bearer", expiresInMinutes);
    }
}
