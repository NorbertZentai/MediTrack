package hu.project.MediWeb.modules.user.controller;

import hu.project.MediWeb.modules.user.dto.UserDTO;
import hu.project.MediWeb.modules.user.dto.UserPublicDTO;
import hu.project.MediWeb.modules.user.dto.AuthLoginResponse;
import hu.project.MediWeb.modules.user.dto.AuthLoginRequest;
import hu.project.MediWeb.modules.user.dto.AuthRegisterRequest;
import jakarta.validation.Valid;
import hu.project.MediWeb.modules.user.entity.User;
import hu.project.MediWeb.modules.user.service.AuthService;
import hu.project.MediWeb.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;
    private final JwtUtil jwtUtil;
    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    public AuthController(AuthService authService, JwtUtil jwtUtil) {
        this.authService = authService;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout() {
        // For JWT, logout is handled client-side by removing the token
        return ResponseEntity.ok("Logout successful");
    }

    @PostMapping("/register")
    public ResponseEntity<UserPublicDTO> registerUser(@Valid @RequestBody AuthRegisterRequest request) {
        User saved = authService.register(request);
        return ResponseEntity.ok(UserPublicDTO.from(saved));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthLoginResponse> loginUser(@Valid @RequestBody AuthLoginRequest credentials) {
        User user = authService.login(credentials.getEmail(), credentials.getPassword());
        String jwtToken = jwtUtil.generateJwtToken(user.getEmail());
        return ResponseEntity.ok(new AuthLoginResponse(UserPublicDTO.from(user), jwtToken, "Bearer"));
    }

    @GetMapping("/me")
    public ResponseEntity<UserDTO> getCurrentUser() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            
            if (authentication != null && authentication.isAuthenticated()) {
                String email = authentication.getName();
                log.debug("auth.me user={} authenticated", email);
                
                User user = authService.findByEmail(email);
                if (user != null) {
                    return ResponseEntity.ok(UserDTO.from(user));
                }
            }
            log.debug("auth.me missing valid authentication");
            return ResponseEntity.status(401).build();
        } catch (Exception e) {
            log.error("auth.me.error msg={}", e.getMessage(), e);
            return ResponseEntity.status(500).build();
        }
    }
}
