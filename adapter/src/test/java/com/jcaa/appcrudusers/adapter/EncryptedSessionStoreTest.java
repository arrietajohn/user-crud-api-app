package com.jcaa.appcrudusers.adapter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.google.gson.Gson;
import com.jcaa.appcrudusers.adapter.cache.AesGcmSessionCipher;
import com.jcaa.appcrudusers.adapter.cache.EncryptedSessionStore;
import com.jcaa.appcrudusers.adapter.cache.SessionBlobStore;
import com.jcaa.appcrudusers.adapter.local.ClearSessionAdapter;
import com.jcaa.appcrudusers.adapter.local.LoadSessionAdapter;
import com.jcaa.appcrudusers.adapter.local.SaveSessionAdapter;
import com.jcaa.appcrudusers.domain.enums.FailureReason;
import com.jcaa.appcrudusers.domain.exception.DomainException;
import com.jcaa.appcrudusers.domain.model.AuthSession;
import com.jcaa.appcrudusers.domain.vo.AccessToken;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.time.Instant;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Stream;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

class EncryptedSessionStoreTest {
    private static final String AES_ALGORITHM = "AES";
    private static final String SENSITIVE_TOKEN = "sensitive-token";
    private static final String BEARER_TOKEN_TYPE = "Bearer";
    private static final String RECEIVED_AT_ISO = "2026-10-26T12:00:00Z";
    private static final String KEYSTORE_UNAVAILABLE_MESSAGE = "unavailable";
    private static final String SIMULATED_STORAGE_ERROR_MESSAGE = "Simulated storage error";
    private static final long EXPIRES_IN_SECONDS = 900L;
    private static final byte UNSUPPORTED_VERSION = 2;
    private static final int TRUNCATED_ENVELOPE_LENGTH = 30;

    private MemoryBlobs blobs;
    private EncryptedSessionStore store;
    private SecretKey key;
    private AesGcmSessionCipher cipher;

    @BeforeEach
    void setUp() throws Exception {
        blobs = new MemoryBlobs();
        key = KeyGenerator.getInstance(AES_ALGORITHM).generateKey();
        cipher = new AesGcmSessionCipher(() -> key);
        store = newStore();
    }

    @Test
    void absentSessionIsEmpty() {
        // Arrange
        final LoadSessionAdapter adapter = new LoadSessionAdapter(store);

        // Act
        final Optional<AuthSession> loaded = adapter.load();

        // Assert
        assertThat(loaded).isEmpty();
    }

    @Test
    void savesEncryptedAndReloadsAcrossStoreInstances() {
        // Arrange
        final AuthSession expectedSession = session();
        final SaveSessionAdapter saveAdapter = new SaveSessionAdapter(store);
        final LoadSessionAdapter loadAdapter = new LoadSessionAdapter(newStore());

        // Act
        saveAdapter.save(expectedSession);
        final AuthSession loaded = loadAdapter.load().orElseThrow(AssertionError::new);

        // Assert
        assertAll(
                () -> assertThat(new String(blobs.bytes, StandardCharsets.UTF_8)).doesNotContain(SENSITIVE_TOKEN),
                () -> assertEquals(SENSITIVE_TOKEN, loaded.getAccessToken().getValue()),
                () -> assertEquals(expectedSession.getExpiresAt(), loaded.getExpiresAt()),
                () -> assertEquals(BEARER_TOKEN_TYPE, loaded.getTokenType()));
    }

    @Test
    void eachWriteUsesANewIv() {
        // Arrange
        final AuthSession activeSession = session();

        // Act
        store.save(activeSession);
        final byte[] first = blobs.bytes.clone();
        store.save(activeSession);

        // Assert
        assertFalse(Arrays.equals(first, blobs.bytes));
    }

    @Test
    void tamperingIsReportedAndDoesNotLeakSecrets() {
        // Arrange
        store.save(session());
        blobs.bytes[blobs.bytes.length - 1] ^= 1;

        // Act
        final DomainException failure = assertThrows(DomainException.class, store::load);

        // Assert
        assertAll(
                () -> assertEquals(FailureReason.SERVICE_UNAVAILABLE, failure.getReason()),
                () -> assertThat(failure.getMessage()).doesNotContain(SENSITIVE_TOKEN),
                () -> assertNull(failure.getCause()));
    }

    @Test
    void wrongKeyCannotReadTheSession() throws Exception {
        // Arrange
        store.save(session());
        key = KeyGenerator.getInstance(AES_ALGORITHM).generateKey();

        // Act
        final DomainException failure = assertThrows(DomainException.class, store::load);

        // Assert
        assertEquals(FailureReason.SERVICE_UNAVAILABLE, failure.getReason());
    }

    @ParameterizedTest
    @MethodSource("invalidEnvelopes")
    void unknownFormatAndTruncatedDataAreReported(final byte[] invalidEnvelope) {
        // Arrange
        blobs.bytes = invalidEnvelope;

        // Act
        final DomainException failure = assertThrows(DomainException.class, store::load);

        // Assert
        assertEquals(FailureReason.SERVICE_UNAVAILABLE, failure.getReason());
    }

    @ParameterizedTest
    @ValueSource(strings = {"null", "{}", "{\"accessToken\":\"sensitive-token\"}", "{bad"})
    void authenticatedButInvalidJsonIsReported(final String json) throws Exception {
        // Arrange
        blobs.bytes = cipher.encrypt(json.getBytes(StandardCharsets.UTF_8));

        // Act
        final DomainException failure = assertThrows(DomainException.class, store::load);

        // Assert
        assertEquals(FailureReason.SERVICE_UNAVAILABLE, failure.getReason());
    }

    @Test
    void clearRemovesTheSessionAndIsIdempotent() {
        // Arrange
        store.save(session());
        final ClearSessionAdapter clearAdapter = new ClearSessionAdapter(store);

        // Act
        clearAdapter.clear();
        final Optional<AuthSession> loadedAfterFirstClear = store.load();
        clearAdapter.clear();

        // Assert
        assertAll(
                () -> assertThat(loadedAfterFirstClear).isEmpty(),
                () -> assertNull(blobs.bytes));
    }

    @Test
    void readWriteAndClearFailuresAreReported() {
        // Arrange
        blobs.fail = true;
        final AuthSession activeSession = session();

        // Act
        final DomainException loadFailure = assertThrows(DomainException.class, store::load);
        final DomainException saveFailure = assertThrows(DomainException.class, () -> store.save(activeSession));
        final DomainException clearFailure = assertThrows(DomainException.class, store::clear);

        // Assert
        assertAll(
                () -> assertEquals(FailureReason.SERVICE_UNAVAILABLE, loadFailure.getReason()),
                () -> assertEquals(FailureReason.SERVICE_UNAVAILABLE, saveFailure.getReason()),
                () -> assertEquals(FailureReason.SERVICE_UNAVAILABLE, clearFailure.getReason()));
    }

    @Test
    void unavailableKeystoreIsReported() {
        // Arrange
        final AesGcmSessionCipher broken = new AesGcmSessionCipher(() -> {
            throw new GeneralSecurityException(KEYSTORE_UNAVAILABLE_MESSAGE);
        });
        final EncryptedSessionStore unavailable = new EncryptedSessionStore(
                blobs,
                broken,
                new Gson());
        final AuthSession activeSession = session();

        // Act
        final DomainException failure = assertThrows(DomainException.class, () -> unavailable.save(activeSession));

        // Assert
        assertAll(
                () -> assertEquals(FailureReason.SERVICE_UNAVAILABLE, failure.getReason()),
                () -> assertNull(blobs.bytes));
    }

    private static Stream<byte[]> invalidEnvelopes() {
        return Stream.of(
                new byte[0],
                new byte[] {UNSUPPORTED_VERSION},
                new byte[TRUNCATED_ENVELOPE_LENGTH]);
    }

    private EncryptedSessionStore newStore() {
        return new EncryptedSessionStore(blobs, cipher, new Gson());
    }

    private static AuthSession session() {
        return new AuthSession(
                new AccessToken(SENSITIVE_TOKEN),
                BEARER_TOKEN_TYPE,
                EXPIRES_IN_SECONDS,
                Instant.parse(RECEIVED_AT_ISO));
    }

    private static final class MemoryBlobs implements SessionBlobStore {
        private byte[] bytes;
        private boolean fail;

        @Override
        public Optional<byte[]> read() throws IOException {
            check();
            return Optional.ofNullable(bytes).map(byte[]::clone);
        }

        @Override
        public void write(final byte[] value) throws IOException {
            check();
            bytes = value.clone();
        }

        @Override
        public void clear() throws IOException {
            check();
            bytes = null;
        }

        private void check() throws IOException {
            if (fail) {
                throw new IOException(SIMULATED_STORAGE_ERROR_MESSAGE);
            }
        }
    }
}
