package com.jcaa.appcrudusers.adapter.remote;

import com.jcaa.appcrudusers.adapter.http.ApiExecutor;
import com.jcaa.appcrudusers.adapter.http.SessionAuthorization;
import com.jcaa.appcrudusers.adapter.http.UsersApi;
import com.jcaa.appcrudusers.adapter.mapper.UserMapper;
import com.jcaa.appcrudusers.domain.model.User;
import com.jcaa.appcrudusers.domain.model.UserUpdate;
import com.jcaa.appcrudusers.domain.port.out.UpdateUserPort;
import com.jcaa.appcrudusers.domain.vo.UserId;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public final class UpdateUserAdapter implements UpdateUserPort {
    private final UsersApi api;
    private final ApiExecutor executor;
    private final SessionAuthorization authorization;

    @Override
    public User update(final UserId id, final UserUpdate update) {
        return executor.execute(
                api.update(authorization.requireHeader(), id.getValue(), UserMapper.toRequest(update)),
                UserMapper::toDomain);
    }
}
