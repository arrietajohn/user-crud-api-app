package com.jcaa.appcrudusers.adapter.dto.response;

import lombok.Getter;

@Getter
public final class ApiErrorResponse {
    private Integer status;
    private String message;
}
