package com.jcaa.appcrudusers.domain.exception;

import com.jcaa.appcrudusers.domain.enums.FailureReason;

public final class ValidationException extends DomainException {
    private final String field;

    public ValidationException(String field, String message) {
        super(FailureReason.VALIDATION, message);
        this.field = field;
    }

    public String getField() { return field; }
}
