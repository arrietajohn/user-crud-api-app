package com.jcaa.appcrudusers.adapter.dto.cache;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public final class CachedSession {
    private final String accessToken;
    private final String tokenType;
    private final Long expiresInSeconds;
    private final String receivedAt;
}
