package com.jcaa.appcrudusers.adapter.dto.response;

import lombok.Getter;

@Getter
public final class LoginResponse {
    private String accessToken;
    private String tokenType;
    private Long expiresIn;
}
