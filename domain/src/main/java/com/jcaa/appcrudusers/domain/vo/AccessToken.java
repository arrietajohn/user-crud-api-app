package com.jcaa.appcrudusers.domain.vo;

/** Opaque token: signature validation and authentication belong to the server. */
public final class AccessToken {
    private final String value;

    public AccessToken(String value) {
        this.value = ValueValidation.required("accessToken", value);
    }

    public String getValue() { return value; }
    @Override public String toString() { return "[REDACTED]"; }
}
