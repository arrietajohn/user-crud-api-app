package com.jcaa.appcrudusers.domain.vo;

import com.jcaa.appcrudusers.domain.exception.ValidationException;
import java.util.regex.Pattern;

/** Immutable value; preserves the server representation without case conversion. */
public final class Email {
    private static final Pattern FORMAT = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");
    private final String value;

    public Email(String value) {
        ValueValidation.required("email", value);
        if (!FORMAT.matcher(value).matches()) {
            throw new ValidationException("email", "El correo debe tener un formato valido.");
        }
        this.value = value;
    }

    public String getValue() { return value; }

    @Override public boolean equals(Object other) {
        return this == other || (other instanceof Email
                && value.equals(((Email) other).value));
    }

    @Override public int hashCode() { return value.hashCode(); }
    @Override public String toString() { return value; }
}
