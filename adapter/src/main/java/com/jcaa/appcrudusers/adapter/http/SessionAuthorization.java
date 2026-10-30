package com.jcaa.appcrudusers.adapter.http;

import com.jcaa.appcrudusers.domain.enums.FailureReason;
import com.jcaa.appcrudusers.domain.exception.DomainException;
import com.jcaa.appcrudusers.domain.model.AuthSession;
import com.jcaa.appcrudusers.domain.port.out.LoadSessionPort;
import java.time.Clock;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public final class SessionAuthorization {
    private static final String HEADER_SEPARATOR = " ";
    private static final String UNAUTHENTICATED_MESSAGE = "Inicia sesion para continuar.";

    private final LoadSessionPort sessions;
    private final Clock clock;

    public String requireHeader() {
        final AuthSession session = sessions.load().orElseThrow(SessionAuthorization::unauthenticated);
        if (session.isExpired(clock.instant())) {
            throw unauthenticated();
        }
        return session.getTokenType() + HEADER_SEPARATOR + session.getAccessToken().getValue();
    }

    private static DomainException unauthenticated() {
        return new DomainException(FailureReason.UNAUTHENTICATED, UNAUTHENTICATED_MESSAGE);
    }
}
