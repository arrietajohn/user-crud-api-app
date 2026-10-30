package com.jcaa.appcrudusers.adapter.cache;

import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.Key;
import java.security.KeyStore;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

public final class AndroidKeystoreKeyProvider implements SecretKeyProvider {
    private static final String KEYSTORE_PROVIDER = "AndroidKeyStore";
    private static final String KEY_ALIAS = "appcrudusers.session.v1";
    private static final String KEYSTORE_OPEN_ERROR_MESSAGE = "No se pudo abrir Android Keystore.";
    private static final int KEY_SIZE_BITS = 256;

    @Override
    public synchronized SecretKey getOrCreate() throws GeneralSecurityException {
        try {
            final KeyStore store = KeyStore.getInstance(KEYSTORE_PROVIDER);
            store.load(null);
            final Key existingKey = store.getKey(KEY_ALIAS, null);
            if (existingKey instanceof SecretKey) {
                return (SecretKey) existingKey;
            }
            final KeyGenerator generator = KeyGenerator.getInstance(
                    KeyProperties.KEY_ALGORITHM_AES,
                    KEYSTORE_PROVIDER);
            generator.init(new KeyGenParameterSpec.Builder(
                    KEY_ALIAS,
                    KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .setKeySize(KEY_SIZE_BITS)
                    .setRandomizedEncryptionRequired(true)
                    .build());
            return generator.generateKey();
        } catch (final IOException exception) {
            throw new GeneralSecurityException(KEYSTORE_OPEN_ERROR_MESSAGE, exception);
        }
    }
}
