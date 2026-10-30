package com.jcaa.appcrudusers.adapter.http;

import com.jcaa.appcrudusers.adapter.dto.request.CreateUserRequest;
import com.jcaa.appcrudusers.adapter.dto.request.LoginRequest;
import com.jcaa.appcrudusers.adapter.dto.request.UpdateUserRequest;
import com.jcaa.appcrudusers.adapter.dto.response.LoginResponse;
import com.jcaa.appcrudusers.adapter.dto.response.UserResponse;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface UsersApi {
    String AUTHORIZATION_HEADER = "Authorization";
    String ID_PATH_PARAMETER = "id";
    String LOGIN_ENDPOINT = "api/auth/login";
    String USERS_ENDPOINT = "api/users";
    String USER_BY_ID_ENDPOINT = "api/users/{id}";

    @POST(LOGIN_ENDPOINT)
    Call<LoginResponse> login(@Body LoginRequest request);

    @POST(USERS_ENDPOINT)
    Call<UserResponse> create(@Body CreateUserRequest request);

    @GET(USER_BY_ID_ENDPOINT)
    Call<UserResponse> getById(
            @Header(AUTHORIZATION_HEADER) String authorization,
            @Path(ID_PATH_PARAMETER) String id);

    @GET(USERS_ENDPOINT)
    Call<List<UserResponse>> list(@Header(AUTHORIZATION_HEADER) String authorization);

    @PUT(USER_BY_ID_ENDPOINT)
    Call<UserResponse> update(
            @Header(AUTHORIZATION_HEADER) String authorization,
            @Path(ID_PATH_PARAMETER) String id,
            @Body UpdateUserRequest request);

    @DELETE(USER_BY_ID_ENDPOINT)
    Call<Void> delete(
            @Header(AUTHORIZATION_HEADER) String authorization,
            @Path(ID_PATH_PARAMETER) String id);
}
