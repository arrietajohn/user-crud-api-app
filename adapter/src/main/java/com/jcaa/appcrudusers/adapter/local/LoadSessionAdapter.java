package com.jcaa.appcrudusers.adapter.local;

import com.jcaa.appcrudusers.adapter.cache.EncryptedSessionStore;
import com.jcaa.appcrudusers.domain.model.AuthSession;
import com.jcaa.appcrudusers.domain.port.out.LoadSessionPort;
import java.util.Optional;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public final class LoadSessionAdapter implements LoadSessionPort {
    private final EncryptedSessionStore store;

    @Override
    public Optional<AuthSession> load() {
        return store.load();
    }
}
