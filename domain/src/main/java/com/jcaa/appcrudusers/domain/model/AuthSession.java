package com.jcaa.appcrudusers.domain.model;

import com.jcaa.appcrudusers.domain.exception.ValidationException;
import com.jcaa.appcrudusers.domain.vo.AccessToken;
import java.time.Instant;
import java.util.Objects;

/** Internal duration in seconds; the adapter must verify and convert the API unit. */
public final class AuthSession {
    private final AccessToken accessToken;
    private final String tokenType;
    private final long expiresInSeconds;
    private final Instant receivedAt;

    public AuthSession(AccessToken accessToken, String tokenType,
                       long expiresInSeconds, Instant receivedAt) {
        this.accessToken = Objects.requireNonNull(accessToken, "accessToken");
        if (!"Bearer".equalsIgnoreCase(tokenType)) {
            throw new ValidationException("tokenType", "Tipo de token no compatible.");
        }
        if (expiresInSeconds <= 0) {
            throw new ValidationException("expiresIn", "La vigencia debe ser positiva.");
        }
        this.tokenType = "Bearer";
        this.expiresInSeconds = expiresInSeconds;
        this.receivedAt = Objects.requireNonNull(receivedAt, "receivedAt");
        try {
            receivedAt.plusSeconds(expiresInSeconds);
        } catch (java.time.DateTimeException | ArithmeticException e) {
            throw new ValidationException("expiresIn", "La vigencia esta fuera de rango.");
        }
    }

    public AccessToken getAccessToken() { return accessToken; }
    public String getTokenType() { return tokenType; }
    public long getExpiresInSeconds() { return expiresInSeconds; }
    public Instant getReceivedAt() { return receivedAt; }
    public Instant getExpiresAt() { return receivedAt.plusSeconds(expiresInSeconds); }
    public boolean isExpired(Instant now) {
        return !Objects.requireNonNull(now, "now").isBefore(getExpiresAt());
    }
}
