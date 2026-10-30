package com.jcaa.appcrudusers.adapter.dto.request;

import java.util.Optional;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public final class UpdateUserRequest {
    private final String name;
    private final String email;
    // Nullable on purpose: Gson omits it so the backend keeps the current password.
    private final String password;
    private final String role;
    private final String status;

    public Optional<String> getPassword() {
        return Optional.ofNullable(password);
    }
}
