package com.jcaa.appcrudusers.adapter.dto.request;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public final class LoginRequest {
    private final String email;
    private final String password;
}
