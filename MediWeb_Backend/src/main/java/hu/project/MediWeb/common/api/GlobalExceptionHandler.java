package hu.project.MediWeb.common.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.servlet.NoHandlerFoundException;
import jakarta.validation.ConstraintViolationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;

import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException ex, WebRequest request) {
        var details = ex.getBindingResult().getFieldErrors().stream()
                .map(err -> err.getField() + ": " + err.getDefaultMessage())
                .collect(Collectors.toList());
        String path = extractPath(request);
        ApiError body = ApiError.of(HttpStatus.BAD_REQUEST.value(), "Bad Request", ErrorCode.VALIDATION_ERROR, "Validation failed", path, details);
        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiError> handleConstraintViolation(ConstraintViolationException ex, WebRequest request) {
        var details = ex.getConstraintViolations().stream()
                .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                .collect(Collectors.toList());
        String path = extractPath(request);
        ApiError body = ApiError.of(HttpStatus.BAD_REQUEST.value(), "Bad Request", ErrorCode.CONSTRAINT_VIOLATION, "Validation failed", path, details);
        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiError> handleIllegalArgument(IllegalArgumentException ex, WebRequest request) {
        String path = extractPath(request);
        ApiError body = ApiError.of(HttpStatus.BAD_REQUEST.value(), "Bad Request", ErrorCode.ILLEGAL_ARGUMENT, ex.getMessage(), path);
        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ApiError> handleResponseStatus(ResponseStatusException ex, WebRequest request) {
        String path = extractPath(request);
        HttpStatusCode statusCode = ex.getStatusCode();
        String error = (statusCode instanceof HttpStatus hs) ? hs.getReasonPhrase() : String.valueOf(statusCode.value());
        ErrorCode code = mapStatusToCode(statusCode.value());
        ApiError body = ApiError.of(statusCode.value(), error, code, ex.getReason(), path);
        return ResponseEntity.status(statusCode).body(body);
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ApiError> handleNoHandler(NoHandlerFoundException ex, WebRequest request) {
        String path = extractPath(request);
        ApiError body = ApiError.of(HttpStatus.NOT_FOUND.value(), "Not Found", ErrorCode.NOT_FOUND, "Resource not found", path);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGeneric(Exception ex, WebRequest request) {
        String path = extractPath(request);
        log.error("unhandled.exception path={} type={} msg={}", path, ex.getClass().getSimpleName(), ex.getMessage(), ex);
        ApiError body = ApiError.of(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Internal Server Error", ErrorCode.INTERNAL_ERROR, "Unexpected server error", path);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }

    private String extractPath(WebRequest request) {
        if (request instanceof ServletWebRequest swr) {
            return swr.getRequest().getRequestURI();
        }
        return "";
    }

    private ErrorCode mapStatusToCode(int status) {
        return switch (status) {
            case 400 -> ErrorCode.BAD_REQUEST;
            case 401 -> ErrorCode.AUTH_FAILED;
            case 403 -> ErrorCode.FORBIDDEN;
            case 404 -> ErrorCode.NOT_FOUND;
            case 409 -> ErrorCode.DUPLICATE_RESOURCE;
            case 429 -> ErrorCode.RATE_LIMITED;
            case 500, 502, 503, 504 -> ErrorCode.INTERNAL_ERROR;
            default -> ErrorCode.UNKNOWN;
        };
    }
}
