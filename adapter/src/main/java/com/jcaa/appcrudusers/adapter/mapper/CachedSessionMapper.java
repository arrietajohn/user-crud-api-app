package com.jcaa.appcrudusers.adapter.mapper;

import com.jcaa.appcrudusers.adapter.dto.cache.CachedSession;
import com.jcaa.appcrudusers.domain.model.AuthSession;
import com.jcaa.appcrudusers.domain.vo.AccessToken;
import java.time.Instant;
import java.util.Objects;
import lombok.experimental.UtilityClass;

@UtilityClass
public class CachedSessionMapper {
    public static CachedSession toCache(final AuthSession session) {
        return new CachedSession(
                session.getAccessToken().getValue(),
                session.getTokenType(),
                session.getExpiresInSeconds(),
                session.getReceivedAt().toString());
    }

    public static AuthSession toDomain(final CachedSession cached) {
        return new AuthSession(
                new AccessToken(cached.getAccessToken()),
                cached.getTokenType(),
                Objects.requireNonNull(cached.getExpiresInSeconds()),
                Instant.parse(cached.getReceivedAt()));
    }
}
