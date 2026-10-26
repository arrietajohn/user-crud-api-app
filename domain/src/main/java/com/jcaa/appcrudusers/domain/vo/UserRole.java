package com.jcaa.appcrudusers.domain.vo;


/** Immutable value; preserves the server representation without case conversion. */
public final class UserRole {
    private final String value;

    public UserRole(String value) {
        ValueValidation.required("role", value);
        this.value = value;
    }

    public String getValue() { return value; }

    @Override public boolean equals(Object other) {
        return this == other || (other instanceof UserRole
                && value.equals(((UserRole) other).value));
    }

    @Override public int hashCode() { return value.hashCode(); }
    @Override public String toString() { return value; }
}
