package com.jcaa.appcrudusers.adapter.remote;

import com.jcaa.appcrudusers.adapter.http.ApiExecutor;
import com.jcaa.appcrudusers.adapter.http.UsersApi;
import com.jcaa.appcrudusers.adapter.mapper.UserMapper;
import com.jcaa.appcrudusers.domain.model.User;
import com.jcaa.appcrudusers.domain.model.UserRegistration;
import com.jcaa.appcrudusers.domain.port.out.CreateUserPort;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public final class CreateUserAdapter implements CreateUserPort {
    private final UsersApi api;
    private final ApiExecutor executor;

    @Override
    public User create(final UserRegistration registration) {
        return executor.execute(api.create(UserMapper.toRequest(registration)), UserMapper::toDomain);
    }
}
