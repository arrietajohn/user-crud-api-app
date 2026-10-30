package com.jcaa.appcrudusers.adapter.remote;

import com.jcaa.appcrudusers.adapter.http.ApiExecutor;
import com.jcaa.appcrudusers.adapter.http.SessionAuthorization;
import com.jcaa.appcrudusers.adapter.http.UsersApi;
import com.jcaa.appcrudusers.adapter.mapper.UserMapper;
import com.jcaa.appcrudusers.domain.model.User;
import com.jcaa.appcrudusers.domain.port.out.ListUsersPort;
import java.util.List;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public final class ListUsersAdapter implements ListUsersPort {
    private final UsersApi api;
    private final ApiExecutor executor;
    private final SessionAuthorization authorization;

    @Override
    public List<User> list() {
        return executor.execute(api.list(authorization.requireHeader()), UserMapper::toDomain);
    }
}
