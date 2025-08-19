package hu.project.MediWeb.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import hu.project.MediWeb.common.api.ApiError;
import hu.project.MediWeb.common.api.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class JsonAccessDeniedHandler implements AccessDeniedHandler {

    @Autowired
    private ObjectMapper objectMapper; // use Spring-configured mapper

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        String path = request.getRequestURI();
        ApiError body = ApiError.of(403, "Forbidden", ErrorCode.FORBIDDEN, "Access denied", path);
        response.getWriter().write(objectMapper.writeValueAsString(body));
    }
}
