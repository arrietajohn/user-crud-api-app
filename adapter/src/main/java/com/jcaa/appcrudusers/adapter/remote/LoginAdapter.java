package com.jcaa.appcrudusers.adapter.remote;

import com.jcaa.appcrudusers.adapter.http.ApiExecutor;
import com.jcaa.appcrudusers.adapter.http.UsersApi;
import com.jcaa.appcrudusers.adapter.mapper.AuthMapper;
import com.jcaa.appcrudusers.domain.model.AuthSession;
import com.jcaa.appcrudusers.domain.model.Credentials;
import com.jcaa.appcrudusers.domain.port.out.LoginPort;
import java.time.Clock;
import java.time.Instant;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public final class LoginAdapter implements LoginPort {
    private final UsersApi api;
    private final ApiExecutor executor;
    private final Clock clock;

    @Override
    public AuthSession login(final Credentials credentials) {
        final Instant requestStartedAt = clock.instant();
        return executor.execute(
                api.login(AuthMapper.toRequest(credentials)),
                response -> AuthMapper.toDomain(response, requestStartedAt));
    }
}
