package com.jcaa.appcrudusers.adapter.http;

import com.google.gson.Gson;
import com.jcaa.appcrudusers.adapter.dto.response.ApiErrorResponse;
import com.jcaa.appcrudusers.domain.enums.FailureReason;
import com.jcaa.appcrudusers.domain.exception.DomainException;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import okhttp3.ResponseBody;

@RequiredArgsConstructor
public final class ApiErrorMapper {
    private static final int HTTP_BAD_REQUEST = 400;
    private static final int HTTP_UNAUTHORIZED = 401;
    private static final int HTTP_FORBIDDEN = 403;
    private static final int HTTP_NOT_FOUND = 404;
    private static final int HTTP_CONFLICT = 409;
    private static final int HTTP_UNPROCESSABLE_ENTITY = 422;
    private static final int HTTP_TOO_MANY_REQUESTS = 429;
    private static final int HTTP_BAD_GATEWAY = 502;
    private static final int HTTP_SERVICE_UNAVAILABLE = 503;
    private static final int HTTP_GATEWAY_TIMEOUT = 504;

    private static final String UNAUTHENTICATED_MESSAGE = "La sesion no es valida. Inicia sesion.";
    private static final String FORBIDDEN_MESSAGE = "No tienes permiso para esta operacion.";
    private static final String NOT_FOUND_MESSAGE = "El usuario solicitado no existe.";
    private static final String CONFLICT_MESSAGE = "El ID o correo ya esta registrado.";
    private static final String SERVICE_UNAVAILABLE_MESSAGE = "El servicio no esta disponible.";
    private static final String UNEXPECTED_RESPONSE_MESSAGE = "La API devolvio una respuesta inesperada.";
    private static final String DEFAULT_VALIDATION_MESSAGE = "La API rechazo los datos enviados.";
    private static final String INVALID_EMAIL_MESSAGE = "Verifica el correo enviado.";
    private static final String INVALID_PASSWORD_MESSAGE = "Verifica la contrasena enviada.";
    private static final String EMAIL_HINT_EN = "email";
    private static final String EMAIL_HINT_ES = "correo";
    private static final String PASSWORD_HINT_EN = "password";
    private static final String PASSWORD_HINT_ES = "contrase";
    private static final String NO_REMOTE_MESSAGE = "";

    private final Gson gson;

    public DomainException map(final int status, final ResponseBody body) {
        // HTTP status is authoritative; the optional JSON status is not trusted.
        switch (status) {
            case HTTP_BAD_REQUEST:
            case HTTP_UNPROCESSABLE_ENTITY:
                return new DomainException(FailureReason.VALIDATION, validationMessage(parseErrorMessage(body)));
            case HTTP_UNAUTHORIZED:
                return new DomainException(FailureReason.UNAUTHENTICATED, UNAUTHENTICATED_MESSAGE);
            case HTTP_FORBIDDEN:
                return new DomainException(FailureReason.FORBIDDEN, FORBIDDEN_MESSAGE);
            case HTTP_NOT_FOUND:
                return new DomainException(FailureReason.NOT_FOUND, NOT_FOUND_MESSAGE);
            case HTTP_CONFLICT:
                return new DomainException(FailureReason.CONFLICT, CONFLICT_MESSAGE);
            case HTTP_TOO_MANY_REQUESTS:
            case HTTP_BAD_GATEWAY:
            case HTTP_SERVICE_UNAVAILABLE:
            case HTTP_GATEWAY_TIMEOUT:
                return new DomainException(FailureReason.SERVICE_UNAVAILABLE, SERVICE_UNAVAILABLE_MESSAGE);
            default:
                return new DomainException(FailureReason.UNEXPECTED, UNEXPECTED_RESPONSE_MESSAGE);
        }
    }

    private Optional<String> parseErrorMessage(final ResponseBody body) {
        if (Objects.isNull(body)) {
            return Optional.empty();
        }
        try (ResponseBody closed = body) {
            return Optional.ofNullable(gson.fromJson(closed.charStream(), ApiErrorResponse.class))
                    .map(ApiErrorResponse::getMessage);
        } catch (final RuntimeException ignored) {
            // Proxies and sleeping hosts may return HTML or an empty body.
            return Optional.empty();
        }
    }

    private static String validationMessage(final Optional<String> remoteMessage) {
        // Recognize field hints only; never echo arbitrary remote message text.
        final String message = remoteMessage.map(text -> text.toLowerCase(Locale.ROOT)).orElse(NO_REMOTE_MESSAGE);
        if (message.contains(EMAIL_HINT_EN) || message.contains(EMAIL_HINT_ES)) {
            return INVALID_EMAIL_MESSAGE;
        }
        if (message.contains(PASSWORD_HINT_EN) || message.contains(PASSWORD_HINT_ES)) {
            return INVALID_PASSWORD_MESSAGE;
        }
        return DEFAULT_VALIDATION_MESSAGE;
    }
}
