package hu.project.MediWeb.common.api;

import java.time.Instant;
import java.util.List;

/**
 * Standard API error envelope.
 */
public record ApiError(
        Instant timestamp,
        int status,
        String error,
        String code,
        String message,
        String path,
        List<String> details
) {
    public static ApiError of(int status, String error, ErrorCode code, String message, String path) {
        return new ApiError(Instant.now(), status, error, code.name(), message, path, List.of());
    }
    public static ApiError of(int status, String error, ErrorCode code, String message, String path, List<String> details) {
        return new ApiError(Instant.now(), status, error, code.name(), message, path, details);
    }
}
