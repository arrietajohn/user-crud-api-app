package com.jcaa.appcrudusers.adapter.remote;

import com.jcaa.appcrudusers.adapter.http.ApiExecutor;
import com.jcaa.appcrudusers.adapter.http.SessionAuthorization;
import com.jcaa.appcrudusers.adapter.http.UsersApi;
import com.jcaa.appcrudusers.domain.port.out.DeleteUserPort;
import com.jcaa.appcrudusers.domain.vo.UserId;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public final class DeleteUserAdapter implements DeleteUserPort {
    private final UsersApi api;
    private final ApiExecutor executor;
    private final SessionAuthorization authorization;

    @Override
    public void delete(final UserId id) {
        executor.executeDelete(api.delete(authorization.requireHeader(), id.getValue()));
    }
}
