package com.jcaa.appcrudusers.adapter.remote;

import com.jcaa.appcrudusers.adapter.http.ApiExecutor;
import com.jcaa.appcrudusers.adapter.http.SessionAuthorization;
import com.jcaa.appcrudusers.adapter.http.UsersApi;
import com.jcaa.appcrudusers.adapter.mapper.UserMapper;
import com.jcaa.appcrudusers.domain.model.User;
import com.jcaa.appcrudusers.domain.port.out.GetUserByIdPort;
import com.jcaa.appcrudusers.domain.vo.UserId;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public final class GetUserByIdAdapter implements GetUserByIdPort {
    private final UsersApi api;
    private final ApiExecutor executor;
    private final SessionAuthorization authorization;

    @Override
    public User getById(final UserId id) {
        return executor.execute(api.getById(authorization.requireHeader(), id.getValue()), UserMapper::toDomain);
    }
}
