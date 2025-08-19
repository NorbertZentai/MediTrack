package hu.project.MediWeb.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import hu.project.MediWeb.common.api.ApiError;
import hu.project.MediWeb.common.api.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class JsonAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Autowired
    private ObjectMapper objectMapper; // use Spring-configured mapper (JavaTimeModule registered)

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        String path = request.getRequestURI();
        ApiError body = ApiError.of(401, "Unauthorized", ErrorCode.AUTH_FAILED, "Authentication required", path);
        response.getWriter().write(objectMapper.writeValueAsString(body));
    }
}
