package com.jcaa.appcrudusers.adapter.local;

import com.jcaa.appcrudusers.adapter.cache.EncryptedSessionStore;
import com.jcaa.appcrudusers.domain.port.out.ClearSessionPort;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public final class ClearSessionAdapter implements ClearSessionPort {
    private final EncryptedSessionStore store;

    @Override
    public void clear() {
        store.clear();
    }
}
