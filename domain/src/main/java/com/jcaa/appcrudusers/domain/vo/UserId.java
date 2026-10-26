package com.jcaa.appcrudusers.domain.vo;


/** Immutable value; preserves the server representation without case conversion. */
public final class UserId {
    private final String value;

    public UserId(String value) {
        ValueValidation.required("id", value);
        this.value = value;
    }

    public String getValue() { return value; }

    @Override public boolean equals(Object other) {
        return this == other || (other instanceof UserId
                && value.equals(((UserId) other).value));
    }

    @Override public int hashCode() { return value.hashCode(); }
    @Override public String toString() { return value; }
}
