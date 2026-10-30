package com.jcaa.appcrudusers.adapter.dto.request;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public final class CreateUserRequest {
    private final String id;
    private final String name;
    private final String email;
    private final String password;
    private final String role;
}
