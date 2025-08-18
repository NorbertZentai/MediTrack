package hu.project.MediWeb.modules.user.controller;

import hu.project.MediWeb.modules.user.dto.UserDTO;
import hu.project.MediWeb.modules.user.dto.UserPublicDTO;
import hu.project.MediWeb.modules.user.dto.AuthLoginResponse;
import hu.project.MediWeb.modules.user.entity.User;
import hu.project.MediWeb.modules.user.service.AuthService;
import hu.project.MediWeb.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;
    private final JwtUtil jwtUtil;

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
    public ResponseEntity<UserPublicDTO> registerUser(@RequestBody User user) {
        User saved = authService.register(user);
        return ResponseEntity.ok(UserPublicDTO.from(saved));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthLoginResponse> loginUser(@RequestBody Map<String, String> credentials) {
        String username = credentials.get("email");
        String password = credentials.get("password");
        User user = authService.login(username, password);
        String jwtToken = jwtUtil.generateJwtToken(user.getEmail());
        return ResponseEntity.ok(new AuthLoginResponse(UserPublicDTO.from(user), jwtToken, "Bearer"));
    }

    @GetMapping("/me")
    public ResponseEntity<UserDTO> getCurrentUser() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            
            if (authentication != null && authentication.isAuthenticated()) {
                String email = authentication.getName();
                System.out.println("🔍 JWT authenticated user: " + email);
                
                User user = authService.findByEmail(email);
                if (user != null) {
                    return ResponseEntity.ok(UserDTO.from(user));
                }
            }
            
            System.out.println("❌ No valid JWT authentication found");
            return ResponseEntity.status(401).build();
        } catch (Exception e) {
            System.err.println("/auth/me error: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).build();
        }
    }
}
