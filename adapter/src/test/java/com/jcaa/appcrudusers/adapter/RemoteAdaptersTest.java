 package com.jcaa.appcrudusers.adapter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.jcaa.appcrudusers.adapter.config.HttpClientFactory;
import com.jcaa.appcrudusers.adapter.http.ApiErrorMapper;
import com.jcaa.appcrudusers.adapter.http.ApiExecutor;
import com.jcaa.appcrudusers.adapter.http.ApiJsonConverterFactory;
import com.jcaa.appcrudusers.adapter.http.SessionAuthorization;
import com.jcaa.appcrudusers.adapter.http.UsersApi;
import com.jcaa.appcrudusers.adapter.remote.CreateUserAdapter;
import com.jcaa.appcrudusers.adapter.remote.DeleteUserAdapter;
import com.jcaa.appcrudusers.adapter.remote.GetUserByIdAdapter;
import com.jcaa.appcrudusers.adapter.remote.ListUsersAdapter;
import com.jcaa.appcrudusers.adapter.remote.LoginAdapter;
import com.jcaa.appcrudusers.adapter.remote.UpdateUserAdapter;
import com.jcaa.appcrudusers.domain.enums.FailureReason;
import com.jcaa.appcrudusers.domain.exception.DomainException;
import com.jcaa.appcrudusers.domain.model.AuthSession;
import com.jcaa.appcrudusers.domain.model.Credentials;
import com.jcaa.appcrudusers.domain.model.User;
import com.jcaa.appcrudusers.domain.model.UserRegistration;
import com.jcaa.appcrudusers.domain.model.UserUpdate;
import com.jcaa.appcrudusers.domain.port.out.LoadSessionPort;
import com.jcaa.appcrudusers.domain.vo.AccessToken;
import com.jcaa.appcrudusers.domain.vo.Email;
import com.jcaa.appcrudusers.domain.vo.Password;
import com.jcaa.appcrudusers.domain.vo.UserId;
import com.jcaa.appcrudusers.domain.vo.UserName;
import com.jcaa.appcrudusers.domain.vo.UserRole;
import com.jcaa.appcrudusers.domain.vo.UserStatus;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import okhttp3.mockwebserver.SocketPolicy;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import retrofit2.Retrofit;

class RemoteAdaptersTest {
    private static final Instant NOW = Instant.parse("2026-10-26T12:00:00Z");
    private static final String USER_JSON =
            "{\"id\":\"usr-1\",\"name\":\"Ana\",\"email\":\"ana@example.com\",\"role\":\"USER\",\"status\":\"ACTIVE\"}";
    private static final String LOGIN_JSON =
            "{\"accessToken\":\"opaque-token\",\"tokenType\":\"Bearer\",\"expiresIn\":900}";
    private static final String ZERO_EXPIRATION_LOGIN_JSON =
            "{\"accessToken\":\"token\",\"tokenType\":\"Bearer\",\"expiresIn\":0}";
    private static final String PARTIAL_USER_JSON = "{\"id\":\"usr-1\"}";
    private static final String NULL_JSON = "null";
    private static final String EMPTY_OBJECT_JSON = "{}";
    private static final String EMPTY_ARRAY_JSON = "[]";
    private static final String INVALID_JSON = "{invalid";
    private static final String JSON_ERROR_WITH_SECRET = "{\"status\":200,\"message\":\"secret123\"}";
    private static final String HTML_ERROR_WITH_SECRET = "<html>secret123</html>";
    private static final String PASSWORD_VALIDATION_ERROR_JSON =
            "{\"status\":400,\"message\":\"password secret123 rejected\"}";
    private static final String PASSWORD_HINT_MESSAGE = "Verifica la contrasena enviada.";
    private static final String USER_ID = "usr-1";
    private static final String USER_NAME = "Ana";
    private static final String USER_EMAIL = "ana@example.com";
    private static final String USER_PASSWORD = "secret123";
    private static final String UPDATED_PASSWORD = "changed123";
    private static final String USER_ROLE = "USER";
    private static final String USER_STATUS = "ACTIVE";
    private static final String OPAQUE_TOKEN = "opaque-token";
    private static final String EXPIRED_TOKEN = "expired";
    private static final String BEARER_TOKEN_TYPE = "Bearer";
    private static final String EXPECTED_BEARER_HEADER = "Bearer opaque-token";
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String CONTENT_TYPE_HEADER = "Content-Type";
    private static final String LOCATION_HEADER = "Location";
    private static final String APPLICATION_JSON = "application/json";
    private static final String ROOT_PATH = "/";
    private static final String REDIRECT_PATH = "/other";
    private static final String LOGIN_PATH = "/api/auth/login";
    private static final String USERS_PATH = "/api/users";
    private static final String USER_BY_ID_PATH = "/api/users/usr-1";
    private static final String ENCODED_USER_BY_ID_PATH = "/api/users/usr%201%2F%3F";
    private static final String SPECIAL_USER_ID = "usr 1/?";
    private static final String POST_METHOD = "POST";
    private static final String GET_METHOD = "GET";
    private static final String PUT_METHOD = "PUT";
    private static final String DELETE_METHOD = "DELETE";
    private static final String ID_FIELD = "id";
    private static final String EMAIL_FIELD = "email";
    private static final String PASSWORD_FIELD = "password";
    private static final String ROLE_FIELD = "role";
    private static final String STATUS_FIELD = "status";
    private static final long EXPIRES_IN_SECONDS = 900L;
    private static final long EXPIRED_DURATION_SECONDS = 60L;
    private static final long READ_TIMEOUT_SECONDS = 1L;
    private static final long CALL_TIMEOUT_SECONDS = 2L;
    private static final int SINGLE_REQUEST = 1;
    private static final int HTTP_OK = 200;
    private static final int HTTP_CREATED = 201;
    private static final int HTTP_NO_CONTENT = 204;
    private static final int HTTP_FOUND = 302;
    private static final int HTTP_BAD_REQUEST = 400;
    private static final int HTTP_UNAUTHORIZED = 401;
    private static final int HTTP_FORBIDDEN = 403;
    private static final int HTTP_NOT_FOUND = 404;
    private static final int HTTP_CONFLICT = 409;
    private static final int HTTP_TOO_MANY_REQUESTS = 429;
    private static final int HTTP_INTERNAL_SERVER_ERROR = 500;
    private static final int HTTP_SERVICE_UNAVAILABLE = 503;

    private final Gson gson = new Gson();
    private final Clock clock = Clock.fixed(NOW, ZoneOffset.UTC);
    private MockWebServer server;
    private UsersApi api;
    private ApiExecutor executor;
    private SessionAuthorization authorization;

    @BeforeEach
    void setUp() throws Exception {
        server = new MockWebServer();
        server.start();
        api = new Retrofit.Builder()
                .baseUrl(server.url(ROOT_PATH))
                .client(HttpClientFactory.newClient().newBuilder()
                        .readTimeout(READ_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                        .callTimeout(CALL_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                        .build())
                .callbackExecutor(Runnable::run)
                .addConverterFactory(new ApiJsonConverterFactory(gson))
                .build()
                .create(UsersApi.class);
        executor = new ApiExecutor(new ApiErrorMapper(gson));
        authorization = new SessionAuthorization(() -> Optional.of(activeSession()), clock);
    }

    @AfterEach
    void tearDown() throws Exception {
        server.shutdown();
    }

    @Test
    void loginSendsCredentialsWithoutBearerAndMapsSeconds() throws Exception {
        // Arrange
        enqueue(HTTP_OK, LOGIN_JSON);
        final LoginAdapter adapter = new LoginAdapter(api, executor, clock);

        // Act
        final AuthSession session = adapter.login(credentials());
        final RecordedRequest request = server.takeRequest();

        // Assert
        final JsonObject json = jsonBody(request);
        assertAll(
                () -> assertEquals(NOW.plusSeconds(EXPIRES_IN_SECONDS), session.getExpiresAt()),
                () -> assertEquals(OPAQUE_TOKEN, session.getAccessToken().getValue()),
                () -> assertEquals(POST_METHOD, request.getMethod()),
                () -> assertEquals(LOGIN_PATH, request.getPath()),
                () -> assertNull(request.getHeader(AUTHORIZATION_HEADER)),
                () -> assertEquals(USER_EMAIL, json.get(EMAIL_FIELD).getAsString()),
                () -> assertEquals(USER_PASSWORD, json.get(PASSWORD_FIELD).getAsString()));
    }

    @ParameterizedTest
    @ValueSource(ints = {HTTP_CREATED, HTTP_OK})
    void createAcceptsBothNewAndIdempotentResponsesWithoutBearer(final int status) throws Exception {
        // Arrange
        enqueue(status, USER_JSON);
        final CreateUserAdapter adapter = new CreateUserAdapter(api, executor);

        // Act
        final User created = adapter.create(registration());
        final RecordedRequest request = server.takeRequest();

        // Assert
        final JsonObject json = jsonBody(request);
        assertAll(
                () -> assertEquals(USER_ID, created.getId().getValue()),
                () -> assertEquals(POST_METHOD, request.getMethod()),
                () -> assertEquals(USERS_PATH, request.getPath()),
                () -> assertNull(request.getHeader(AUTHORIZATION_HEADER)),
                () -> assertEquals(USER_ID, json.get(ID_FIELD).getAsString()),
                () -> assertEquals(USER_ROLE, json.get(ROLE_FIELD).getAsString()),
                () -> assertEquals(USER_PASSWORD, json.get(PASSWORD_FIELD).getAsString()),
                () -> assertFalse(json.has(STATUS_FIELD)));
    }

    @Test
    void getByIdEncodesPathAndSendsBearer() throws Exception {
        // Arrange
        enqueue(HTTP_OK, USER_JSON);
        final GetUserByIdAdapter adapter = new GetUserByIdAdapter(api, executor, authorization);

        // Act
        final User found = adapter.getById(new UserId(SPECIAL_USER_ID));
        final RecordedRequest request = server.takeRequest();

        // Assert
        assertAll(
                () -> assertEquals(USER_EMAIL, found.getEmail().getValue()),
                () -> assertEquals(GET_METHOD, request.getMethod()),
                () -> assertEquals(ENCODED_USER_BY_ID_PATH, request.getPath()),
                () -> assertEquals(EXPECTED_BEARER_HEADER, request.getHeader(AUTHORIZATION_HEADER)));
    }

    @Test
    void listMapsUsersAndSendsBearer() throws Exception {
        // Arrange
        enqueue(HTTP_OK, "[" + USER_JSON + "]");
        final ListUsersAdapter adapter = new ListUsersAdapter(api, executor, authorization);

        // Act
        final List<User> users = adapter.list();
        final RecordedRequest request = server.takeRequest();

        // Assert
        assertAll(
                () -> assertThat(users)
                        .extracting(user -> user.getId().getValue())
                        .containsExactly(USER_ID),
                () -> assertEquals(GET_METHOD, request.getMethod()),
                () -> assertEquals(USERS_PATH, request.getPath()),
                () -> assertEquals(EXPECTED_BEARER_HEADER, request.getHeader(AUTHORIZATION_HEADER)));
    }

    @Test
    void listAllowsEmptyResponse() {
        // Arrange
        enqueue(HTTP_OK, EMPTY_ARRAY_JSON);
        final ListUsersAdapter adapter = new ListUsersAdapter(api, executor, authorization);

        // Act
        final List<User> users = adapter.list();

        // Assert
        assertThat(users).isEmpty();
    }

    @Test
    void updateOmitsAbsentPassword() throws Exception {
        // Arrange
        enqueue(HTTP_OK, USER_JSON);
        final UpdateUserAdapter adapter = new UpdateUserAdapter(api, executor, authorization);

        // Act
        adapter.update(new UserId(USER_ID), userUpdate(Optional.empty()));
        final RecordedRequest request = server.takeRequest();

        // Assert
        final JsonObject json = jsonBody(request);
        assertAll(
                () -> assertEquals(PUT_METHOD, request.getMethod()),
                () -> assertEquals(USER_BY_ID_PATH, request.getPath()),
                () -> assertEquals(EXPECTED_BEARER_HEADER, request.getHeader(AUTHORIZATION_HEADER)),
                () -> assertEquals(USER_STATUS, json.get(STATUS_FIELD).getAsString()),
                () -> assertFalse(json.has(PASSWORD_FIELD)));
    }

    @Test
    void updateIncludesPresentPassword() throws Exception {
        // Arrange
        enqueue(HTTP_OK, USER_JSON);
        final UpdateUserAdapter adapter = new UpdateUserAdapter(api, executor, authorization);

        // Act
        adapter.update(new UserId(USER_ID), userUpdate(Optional.of(new Password(UPDATED_PASSWORD))));
        final RecordedRequest request = server.takeRequest();

        // Assert
        assertEquals(UPDATED_PASSWORD, jsonBody(request).get(PASSWORD_FIELD).getAsString());
    }

    @Test
    void deleteAcceptsNoContent() throws Exception {
        // Arrange
        server.enqueue(new MockResponse().setResponseCode(HTTP_NO_CONTENT));
        final DeleteUserAdapter adapter = new DeleteUserAdapter(api, executor, authorization);

        // Act
        adapter.delete(new UserId(USER_ID));
        final RecordedRequest request = server.takeRequest();

        // Assert
        assertAll(
                () -> assertEquals(DELETE_METHOD, request.getMethod()),
                () -> assertEquals(USER_BY_ID_PATH, request.getPath()),
                () -> assertEquals(EXPECTED_BEARER_HEADER, request.getHeader(AUTHORIZATION_HEADER)));
    }

    @ParameterizedTest
    @MethodSource("invalidSessions")
    void missingAndExpiredSessionsDoNotCallTheServer(final LoadSessionPort invalidSessions) {
        // Arrange
        final SessionAuthorization invalidAuthorization = new SessionAuthorization(invalidSessions, clock);
        final ListUsersAdapter adapter = new ListUsersAdapter(api, executor, invalidAuthorization);

        // Act
        final DomainException failure = assertThrows(DomainException.class, adapter::list);

        // Assert
        assertAll(
                () -> assertEquals(FailureReason.UNAUTHENTICATED, failure.getReason()),
                () -> assertEquals(0, server.getRequestCount()));
    }

    @ParameterizedTest
    @MethodSource("httpErrors")
    void translatesHttpErrorsAndNeverLeaksRemoteContent(
            final int status,
            final String body,
            final FailureReason expectedReason) {
        // Arrange
        enqueue(status, body);
        final ListUsersAdapter adapter = new ListUsersAdapter(api, executor, authorization);

        // Act
        final DomainException failure = assertThrows(DomainException.class, adapter::list);

        // Assert
        assertAll(
                () -> assertEquals(expectedReason, failure.getReason()),
                () -> assertThat(failure.getMessage()).doesNotContain(USER_PASSWORD));
    }

    @ParameterizedTest
    @ValueSource(strings = {NULL_JSON, EMPTY_OBJECT_JSON, PARTIAL_USER_JSON, INVALID_JSON})
    void malformedSuccessIsAnUnexpectedFailure(final String response) {
        // Arrange
        enqueue(HTTP_OK, response);
        final GetUserByIdAdapter adapter = new GetUserByIdAdapter(api, executor, authorization);
        final UserId userId = new UserId(USER_ID);

        // Act
        final DomainException failure = assertThrows(DomainException.class, () -> adapter.getById(userId));

        // Assert
        assertEquals(FailureReason.UNEXPECTED, failure.getReason());
    }

    @Test
    void validationErrorsProvideSafeFieldHints() {
        // Arrange
        enqueue(HTTP_BAD_REQUEST, PASSWORD_VALIDATION_ERROR_JSON);
        final CreateUserAdapter adapter = new CreateUserAdapter(api, executor);
        final UserRegistration userRegistration = registration();

        // Act
        final DomainException failure = assertThrows(DomainException.class, () -> adapter.create(userRegistration));

        // Assert
        assertAll(
                () -> assertEquals(FailureReason.VALIDATION, failure.getReason()),
                () -> assertEquals(PASSWORD_HINT_MESSAGE, failure.getMessage()));
    }

    @ParameterizedTest
    @ValueSource(strings = {EMPTY_OBJECT_JSON, ZERO_EXPIRATION_LOGIN_JSON})
    void invalidLoginMetadataIsAnUnexpectedFailure(final String response) {
        // Arrange
        enqueue(HTTP_OK, response);
        final LoginAdapter adapter = new LoginAdapter(api, executor, clock);
        final Credentials credentials = credentials();

        // Act
        final DomainException failure = assertThrows(DomainException.class, () -> adapter.login(credentials));

        // Assert
        assertEquals(FailureReason.UNEXPECTED, failure.getReason());
    }

    @Test
    void networkFailureDoesNotRetry() {
        // Arrange
        server.enqueue(new MockResponse().setSocketPolicy(SocketPolicy.DISCONNECT_AFTER_REQUEST));
        final CreateUserAdapter adapter = new CreateUserAdapter(api, executor);
        final UserRegistration userRegistration = registration();

        // Act
        final DomainException failure = assertThrows(DomainException.class, () -> adapter.create(userRegistration));

        // Assert
        assertAll(
                () -> assertEquals(FailureReason.NETWORK, failure.getReason()),
                () -> assertEquals(SINGLE_REQUEST, server.getRequestCount()));
    }

    @Test
    void redirectsAreNotFollowed() {
        // Arrange
        server.enqueue(new MockResponse()
                .setResponseCode(HTTP_FOUND)
                .setHeader(LOCATION_HEADER, server.url(REDIRECT_PATH)));
        final ListUsersAdapter adapter = new ListUsersAdapter(api, executor, authorization);

        // Act
        final DomainException failure = assertThrows(DomainException.class, adapter::list);

        // Assert
        assertAll(
                () -> assertEquals(FailureReason.UNEXPECTED, failure.getReason()),
                () -> assertEquals(SINGLE_REQUEST, server.getRequestCount()));
    }

    @Test
    void deleteRejectsUnexpectedSuccessStatus() {
        // Arrange
        enqueue(HTTP_OK, EMPTY_OBJECT_JSON);
        final DeleteUserAdapter adapter = new DeleteUserAdapter(api, executor, authorization);
        final UserId userId = new UserId(USER_ID);

        // Act
        final DomainException failure = assertThrows(DomainException.class, () -> adapter.delete(userId));

        // Assert
        assertEquals(FailureReason.UNEXPECTED, failure.getReason());
    }

    private static Stream<LoadSessionPort> invalidSessions() {
        final AuthSession expiredSession = new AuthSession(
                new AccessToken(EXPIRED_TOKEN),
                BEARER_TOKEN_TYPE,
                EXPIRED_DURATION_SECONDS,
                NOW.minusSeconds(EXPIRED_DURATION_SECONDS));
        return Stream.of(Optional::empty, () -> Optional.of(expiredSession));
    }

    private static Stream<Arguments> httpErrors() {
        return Stream.of(
                Arguments.of(HTTP_BAD_REQUEST, JSON_ERROR_WITH_SECRET, FailureReason.VALIDATION),
                Arguments.of(HTTP_UNAUTHORIZED, HTML_ERROR_WITH_SECRET, FailureReason.UNAUTHENTICATED),
                Arguments.of(HTTP_FORBIDDEN, JSON_ERROR_WITH_SECRET, FailureReason.FORBIDDEN),
                Arguments.of(HTTP_NOT_FOUND, HTML_ERROR_WITH_SECRET, FailureReason.NOT_FOUND),
                Arguments.of(HTTP_CONFLICT, JSON_ERROR_WITH_SECRET, FailureReason.CONFLICT),
                Arguments.of(HTTP_TOO_MANY_REQUESTS, HTML_ERROR_WITH_SECRET, FailureReason.SERVICE_UNAVAILABLE),
                Arguments.of(HTTP_INTERNAL_SERVER_ERROR, JSON_ERROR_WITH_SECRET, FailureReason.UNEXPECTED),
                Arguments.of(HTTP_SERVICE_UNAVAILABLE, HTML_ERROR_WITH_SECRET, FailureReason.SERVICE_UNAVAILABLE));
    }

    private static AuthSession activeSession() {
        return new AuthSession(
                new AccessToken(OPAQUE_TOKEN),
                BEARER_TOKEN_TYPE,
                EXPIRES_IN_SECONDS,
                NOW);
    }

    private static Credentials credentials() {
        return new Credentials(new Email(USER_EMAIL), new Password(USER_PASSWORD));
    }

    private static UserRegistration registration() {
        return new UserRegistration(
                new UserId(USER_ID),
                new UserName(USER_NAME),
                new Email(USER_EMAIL),
                new Password(USER_PASSWORD),
                new UserRole(USER_ROLE));
    }

    private static UserUpdate userUpdate(final Optional<Password> password) {
        return new UserUpdate(
                new UserName(USER_NAME),
                new Email(USER_EMAIL),
                password,
                new UserRole(USER_ROLE),
                new UserStatus(USER_STATUS));
    }

    private static JsonObject jsonBody(final RecordedRequest request) {
        return JsonParser.parseString(request.getBody().readUtf8()).getAsJsonObject();
    }

    private void enqueue(final int status, final String json) {
        server.enqueue(new MockResponse()
                .setResponseCode(status)
                .setHeader(CONTENT_TYPE_HEADER, APPLICATION_JSON)
                .setBody(json));
    }
}
