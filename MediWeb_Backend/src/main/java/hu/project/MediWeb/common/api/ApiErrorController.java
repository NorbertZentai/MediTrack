package hu.project.MediWeb.common.api;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/** Custom error controller to unify 404 and other default errors into ApiError JSON. */
@Controller
public class ApiErrorController implements ErrorController {

    @RequestMapping("/error")
    public ResponseEntity<ApiError> handleError(HttpServletRequest request) {
        Object statusAttr = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        int status = statusAttr instanceof Integer ? (Integer) statusAttr : 500;
        String path = (String) request.getAttribute(RequestDispatcher.ERROR_REQUEST_URI);
        if (path == null) {
            path = request.getRequestURI();
        }
        HttpStatus httpStatus = HttpStatus.resolve(status);
        if (httpStatus == null) {
            httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
        }
        ErrorCode code = switch (status) {
            case 400 -> ErrorCode.BAD_REQUEST;
            case 401 -> ErrorCode.AUTH_FAILED;
            case 403 -> ErrorCode.FORBIDDEN;
            case 404 -> ErrorCode.NOT_FOUND;
            case 409 -> ErrorCode.DUPLICATE_RESOURCE;
            case 429 -> ErrorCode.RATE_LIMITED;
            case 500, 502, 503, 504 -> ErrorCode.INTERNAL_ERROR;
            default -> ErrorCode.UNKNOWN;
        };
        String message = switch (code) {
            case NOT_FOUND -> "Resource not found";
            case BAD_REQUEST -> "Bad request";
            case FORBIDDEN -> "Forbidden";
            case AUTH_FAILED -> "Authentication required";
            case RATE_LIMITED -> "Rate limit exceeded";
            case DUPLICATE_RESOURCE -> "Conflict";
            case INTERNAL_ERROR -> "Unexpected server error";
            default -> httpStatus.getReasonPhrase();
        };
        ApiError apiError = ApiError.of(httpStatus.value(), httpStatus.getReasonPhrase(), code, message, path);
        return ResponseEntity.status(httpStatus).body(apiError);
    }
}
