package com.jcaa.appcrudusers.adapter.cache;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.Objects;
import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public final class AesGcmSessionCipher {
    private static final String CIPHER_TRANSFORMATION = "AES/GCM/NoPadding";
    private static final String AAD_VALUE = "appcrudusers.session.v1";
    private static final String INCOMPATIBLE_IV_MESSAGE = "IV incompatible.";
    private static final String INCOMPATIBLE_SESSION_FORMAT_MESSAGE = "Formato de sesion incompatible.";
    private static final int VERSION_LENGTH_BYTES = 1;
    private static final int IV_OFFSET = VERSION_LENGTH_BYTES;
    private static final int IV_LENGTH_BYTES = 12;
    private static final int PAYLOAD_OFFSET = VERSION_LENGTH_BYTES + IV_LENGTH_BYTES;
    private static final int GCM_TAG_LENGTH_BYTES = 16;
    private static final int GCM_TAG_LENGTH_BITS = 128;
    private static final int MIN_ENVELOPE_LENGTH_BYTES = PAYLOAD_OFFSET + GCM_TAG_LENGTH_BYTES;
    private static final byte CURRENT_VERSION = 1;
    private static final byte[] ADDITIONAL_AUTHENTICATED_DATA = AAD_VALUE.getBytes(StandardCharsets.UTF_8);

    private final SecretKeyProvider keyProvider;

    public byte[] encrypt(final byte[] plaintext) throws GeneralSecurityException {
        final Cipher cipher = Cipher.getInstance(CIPHER_TRANSFORMATION);
        cipher.init(Cipher.ENCRYPT_MODE, keyProvider.getOrCreate());
        cipher.updateAAD(ADDITIONAL_AUTHENTICATED_DATA);
        final byte[] initializationVector = cipher.getIV();
        if (Objects.isNull(initializationVector) || initializationVector.length != IV_LENGTH_BYTES) {
            throw new GeneralSecurityException(INCOMPATIBLE_IV_MESSAGE);
        }
        final byte[] encrypted = cipher.doFinal(plaintext);
        final byte[] envelope = new byte[PAYLOAD_OFFSET + encrypted.length];
        envelope[0] = CURRENT_VERSION;
        System.arraycopy(initializationVector, 0, envelope, IV_OFFSET, IV_LENGTH_BYTES);
        System.arraycopy(encrypted, 0, envelope, PAYLOAD_OFFSET, encrypted.length);
        return envelope;
    }

    public byte[] decrypt(final byte[] envelope) throws GeneralSecurityException {
        if (Objects.isNull(envelope)
                || envelope.length < MIN_ENVELOPE_LENGTH_BYTES
                || envelope[0] != CURRENT_VERSION) {
            throw new GeneralSecurityException(INCOMPATIBLE_SESSION_FORMAT_MESSAGE);
        }
        final Cipher cipher = Cipher.getInstance(CIPHER_TRANSFORMATION);
        final byte[] initializationVector = Arrays.copyOfRange(envelope, IV_OFFSET, PAYLOAD_OFFSET);
        cipher.init(
                Cipher.DECRYPT_MODE,
                keyProvider.getOrCreate(),
                new GCMParameterSpec(GCM_TAG_LENGTH_BITS, initializationVector));
        cipher.updateAAD(ADDITIONAL_AUTHENTICATED_DATA);
        return cipher.doFinal(envelope, PAYLOAD_OFFSET, envelope.length - PAYLOAD_OFFSET);
    }
}
