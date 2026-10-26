package com.jcaa.appcrudusers.domain.vo;

import com.jcaa.appcrudusers.domain.exception.ValidationException;

/** Plaintext exists only for the outbound request. Never include it in logs or persisted users. */
public final class Password {
    private final String value;

    public Password(String value) {
        ValueValidation.required("password", value);
        if (value.codePointCount(0, value.length()) < 8) {
            throw new ValidationException("password", "La contrasena debe tener al menos 8 caracteres.");
        }
        this.value = value;
    }

    public String getValue() { return value; }
    @Override public String toString() { return "[REDACTED]"; }
}
