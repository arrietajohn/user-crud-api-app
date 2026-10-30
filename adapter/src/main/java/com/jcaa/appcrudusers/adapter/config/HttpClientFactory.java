package com.jcaa.appcrudusers.adapter.config;

import com.google.gson.Gson;
import com.jcaa.appcrudusers.adapter.http.ApiJsonConverterFactory;
import com.jcaa.appcrudusers.adapter.http.UsersApi;
import java.util.concurrent.TimeUnit;
import lombok.experimental.UtilityClass;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;

@UtilityClass
public class HttpClientFactory {
    public static final String BASE_URL = "https://users-management-api-orb0.onrender.com/";

    private static final String HTTPS_REQUIRED_MESSAGE = "La API requiere HTTPS.";
    private static final long CONNECT_TIMEOUT_SECONDS = 20L;
    private static final long READ_TIMEOUT_SECONDS = 90L;
    private static final long WRITE_TIMEOUT_SECONDS = 30L;
    private static final long CALL_TIMEOUT_SECONDS = 100L;

    public static OkHttpClient newClient() {
        return new OkHttpClient.Builder()
                .connectTimeout(CONNECT_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .readTimeout(READ_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .writeTimeout(WRITE_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .callTimeout(CALL_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .retryOnConnectionFailure(false)
                .followRedirects(false)
                .followSslRedirects(false)
                .build();
    }

    public static UsersApi createApi(final Gson gson) {
        final HttpUrl base = HttpUrl.get(BASE_URL);
        if (!base.isHttps()) {
            throw new IllegalArgumentException(HTTPS_REQUIRED_MESSAGE);
        }
        return new Retrofit.Builder()
                .baseUrl(base)
                .client(newClient())
                .addConverterFactory(new ApiJsonConverterFactory(gson))
                .build()
                .create(UsersApi.class);
    }
}
