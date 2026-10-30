package com.jcaa.appcrudusers.adapter.mapper;

import com.jcaa.appcrudusers.adapter.dto.request.LoginRequest;
import com.jcaa.appcrudusers.adapter.dto.response.LoginResponse;
import com.jcaa.appcrudusers.domain.model.AuthSession;
import com.jcaa.appcrudusers.domain.model.Credentials;
import com.jcaa.appcrudusers.domain.vo.AccessToken;
import java.time.Instant;
import java.util.Objects;
import lombok.experimental.UtilityClass;

@UtilityClass
public class AuthMapper {
    public static LoginRequest toRequest(final Credentials credentials) {
        return new LoginRequest(credentials.getEmail().getValue(), credentials.getPassword().getValue());
    }

    public static AuthSession toDomain(final LoginResponse response, final Instant requestStartedAt) {
        // Backend AuthRestController returns JwtTokenService.expirationSeconds().
        return new AuthSession(
                new AccessToken(response.getAccessToken()),
                response.getTokenType(),
                Objects.requireNonNull(response.getExpiresIn()),
                requestStartedAt);
    }
}
