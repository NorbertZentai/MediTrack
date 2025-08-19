package hu.project.MediWeb.common.api;

/** Canonical application error codes for stable client handling. */
public enum ErrorCode {
    VALIDATION_ERROR,
    CONSTRAINT_VIOLATION,
    ILLEGAL_ARGUMENT,
    AUTH_FAILED,
    RATE_LIMITED,
    DUPLICATE_RESOURCE,
    NOT_FOUND,
    FORBIDDEN,
    INTERNAL_ERROR,
    BAD_REQUEST,
    UNKNOWN;
}
