package com.jcaa.appcrudusers.adapter.local;

import com.jcaa.appcrudusers.adapter.cache.EncryptedSessionStore;
import com.jcaa.appcrudusers.domain.model.AuthSession;
import com.jcaa.appcrudusers.domain.port.out.SaveSessionPort;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public final class SaveSessionAdapter implements SaveSessionPort {
    private final EncryptedSessionStore store;

    @Override
    public void save(final AuthSession session) {
        store.save(session);
    }
}
