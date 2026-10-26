package com.jcaa.appcrudusers.domain.vo;

import com.jcaa.appcrudusers.domain.exception.ValidationException;

/** Immutable value; preserves the server representation without case conversion. */
public final class UserName {
    private final String value;

    public UserName(String value) {
        ValueValidation.required("name", value);
        if (value.codePointCount(0, value.length()) < 3) {
            throw new ValidationException("name", "El nombre debe tener al menos 3 caracteres.");
        }
        this.value = value;
    }

    public String getValue() { return value; }

    @Override public boolean equals(Object other) {
        return this == other || (other instanceof UserName
                && value.equals(((UserName) other).value));
    }

    @Override public int hashCode() { return value.hashCode(); }
    @Override public String toString() { return value; }
}
