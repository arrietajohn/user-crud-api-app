package com.jcaa.appcrudusers.domain.enums;

/** Transport-independent failures that an adapter can report. */
public enum FailureReason {
    VALIDATION, UNAUTHENTICATED, FORBIDDEN, NOT_FOUND, CONFLICT,
    NETWORK, SERVICE_UNAVAILABLE, UNEXPECTED
}
