package com.jcaa.appcrudusers.domain.exception;

import com.jcaa.appcrudusers.domain.enums.FailureReason;
import java.util.Objects;

public class DomainException extends RuntimeException {
    private final FailureReason reason;

    public DomainException(FailureReason reason, String message) {
        super(message);
        this.reason = Objects.requireNonNull(reason, "reason");
    }

    public DomainException(FailureReason reason, String message, Throwable cause) {
        super(message, cause);
        this.reason = Objects.requireNonNull(reason, "reason");
    }

    public FailureReason getReason() { return reason; }
}
