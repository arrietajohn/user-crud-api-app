package com.jcaa.appcrudusers.adapter.http;

import com.google.gson.Gson;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Objects;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Converter;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/** Distinguishes a failed download from a complete response with invalid JSON. */
public final class ApiJsonConverterFactory extends Converter.Factory {
    private static final String INCOMPATIBLE_JSON_MESSAGE = "La respuesta JSON es incompatible.";

    private final GsonConverterFactory delegate;

    public ApiJsonConverterFactory(final Gson gson) {
        delegate = GsonConverterFactory.create(Objects.requireNonNull(gson));
    }

    @Override
    public Converter<ResponseBody, ?> responseBodyConverter(
            final Type type,
            final Annotation[] annotations,
            final Retrofit retrofit) {
        final Converter<ResponseBody, ?> converter = Objects.requireNonNull(
                delegate.responseBodyConverter(type, annotations, retrofit));
        return body -> {
            final MediaType mediaType = body.contentType();
            final byte[] downloaded = body.bytes(); // Network failures remain IOException.
            try (ResponseBody buffered = ResponseBody.create(downloaded, mediaType)) {
                return converter.convert(buffered);
            } catch (final IOException | RuntimeException exception) {
                // Never attach parser exceptions which may contain response secrets.
                throw new IllegalStateException(INCOMPATIBLE_JSON_MESSAGE);
            }
        };
    }

    @Override
    public Converter<?, RequestBody> requestBodyConverter(
            final Type type,
            final Annotation[] parameterAnnotations,
            final Annotation[] methodAnnotations,
            final Retrofit retrofit) {
        return delegate.requestBodyConverter(type, parameterAnnotations, methodAnnotations, retrofit);
    }
}
