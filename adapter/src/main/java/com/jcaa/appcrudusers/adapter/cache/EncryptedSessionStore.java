package com.jcaa.appcrudusers.adapter.cache;

import com.google.gson.Gson;
import com.jcaa.appcrudusers.adapter.dto.cache.CachedSession;
import com.jcaa.appcrudusers.adapter.mapper.CachedSessionMapper;
import com.jcaa.appcrudusers.domain.enums.FailureReason;
import com.jcaa.appcrudusers.domain.exception.DomainException;
import com.jcaa.appcrudusers.domain.model.AuthSession;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.Optional;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public final class EncryptedSessionStore {
    private static final String STORAGE_UNAVAILABLE_MESSAGE =
            "No se pudo acceder al almacenamiento seguro de la sesion.";
    private static final byte ZERO_BYTE = 0;

    private final SessionBlobStore blobs;
    private final AesGcmSessionCipher cipher;
    private final Gson gson;

    public synchronized void save(final AuthSession session) {
        final byte[] plaintext = gson.toJson(CachedSessionMapper.toCache(session)).getBytes(StandardCharsets.UTF_8);
        try {
            blobs.write(cipher.encrypt(plaintext));
        } catch (final IOException | GeneralSecurityException | RuntimeException exception) {
            throw unavailable();
        } finally {
            Arrays.fill(plaintext, ZERO_BYTE);
        }
    }

    public synchronized Optional<AuthSession> load() {
        try {
            final Optional<byte[]> encrypted = blobs.read();
            if (!encrypted.isPresent()) {
                return Optional.empty();
            }
            return Optional.of(decryptSession(encrypted.get()));
        } catch (final IOException | GeneralSecurityException | RuntimeException exception) {
            // A corrupt/unreadable cache must not masquerade as an absent session.
            throw unavailable();
        }
    }

    public synchronized void clear() {
        try {
            blobs.clear();
        } catch (final IOException | RuntimeException exception) {
            throw unavailable();
        }
    }

    private AuthSession decryptSession(final byte[] encrypted) throws GeneralSecurityException {
        final byte[] plaintext = cipher.decrypt(encrypted);
        try {
            final CachedSession cached = gson.fromJson(
                    new String(plaintext, StandardCharsets.UTF_8),
                    CachedSession.class);
            return CachedSessionMapper.toDomain(cached);
        } finally {
            Arrays.fill(plaintext, ZERO_BYTE);
        }
    }

    private static DomainException unavailable() {
        // Do not attach crypto/JSON exceptions that might carry token plaintext.
        return new DomainException(FailureReason.SERVICE_UNAVAILABLE, STORAGE_UNAVAILABLE_MESSAGE);
    }
}
