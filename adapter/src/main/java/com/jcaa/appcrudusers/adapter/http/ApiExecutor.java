package com.jcaa.appcrudusers.adapter.http;

import com.jcaa.appcrudusers.domain.enums.FailureReason;
import com.jcaa.appcrudusers.domain.exception.DomainException;
import java.io.IOException;
import java.util.Objects;
import java.util.function.Function;
import lombok.RequiredArgsConstructor;
import retrofit2.Call;
import retrofit2.Response;

@RequiredArgsConstructor
public final class ApiExecutor {
    private static final int HTTP_NO_CONTENT = 204;
    private static final String NETWORK_ERROR_MESSAGE = "No se pudo conectar con el servicio.";
    private static final String INCOMPATIBLE_DATA_MESSAGE = "La API devolvio datos incompatibles.";

    private final ApiErrorMapper errors;

    public <R, T> T execute(final Call<R> call, final Function<R, T> mapper) {
        final R body = send(call).body();
        if (Objects.isNull(body)) {
            throw invalidResponse();
        }
        try {
            return mapper.apply(body);
        } catch (final RuntimeException exception) {
            // A domain validation failure while mapping is malformed REMOTE data.
            throw invalidResponse();
        }
    }

    public void executeDelete(final Call<Void> call) {
        final Response<Void> response = send(call);
        if (response.code() != HTTP_NO_CONTENT) {
            throw invalidResponse();
        }
    }

    private <T> Response<T> send(final Call<T> call) {
        try {
            final Response<T> response = call.execute();
            if (!response.isSuccessful()) {
                throw errors.map(response.code(), response.errorBody());
            }
            return response;
        } catch (final IOException exception) {
            throw new DomainException(FailureReason.NETWORK, NETWORK_ERROR_MESSAGE);
        } catch (final DomainException exception) {
            throw exception;
        } catch (final RuntimeException exception) {
            throw invalidResponse();
        }
    }

    private static DomainException invalidResponse() {
        return new DomainException(FailureReason.UNEXPECTED, INCOMPATIBLE_DATA_MESSAGE);
    }
}
