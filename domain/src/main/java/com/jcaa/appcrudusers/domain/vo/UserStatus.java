package com.jcaa.appcrudusers.domain.vo;


/** Immutable value; preserves the server representation without case conversion. */
public final class UserStatus {
    private final String value;

    public UserStatus(String value) {
        ValueValidation.required("status", value);
        this.value = value;
    }

    public String getValue() { return value; }

    @Override public boolean equals(Object other) {
        return this == other || (other instanceof UserStatus
                && value.equals(((UserStatus) other).value));
    }

    @Override public int hashCode() { return value.hashCode(); }
    @Override public String toString() { return value; }
}
