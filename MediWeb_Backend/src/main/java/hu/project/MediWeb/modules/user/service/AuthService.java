package hu.project.MediWeb.modules.user.service;

import hu.project.MediWeb.modules.user.entity.User;
import hu.project.MediWeb.modules.user.dto.AuthRegisterRequest;
import hu.project.MediWeb.common.security.RateLimiter;
import hu.project.MediWeb.modules.user.enums.UserRole;
import hu.project.MediWeb.modules.user.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

@Service
public class AuthService {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private RateLimiter rateLimiter;

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private static final String PASSWORD_POLICY_REGEX = "^(?=.*[A-Za-z])(?=.*\\d).{8,}$"; // min 8, letter+digit

    public User register(AuthRegisterRequest req) {
        if (!rateLimiter.allow("register:" + req.getEmail())) {
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "Túl sok regisztrációs próbálkozás. Próbáld később.");
        }
        log.info("auth.register attempt email={}", req.getEmail());
        if (userRepository.findByEmail(req.getEmail()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ez az email cím már használatban van!");
        }
        if (userRepository.findByName(req.getName()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ez a felhasználónév már használatban van!");
        }
        if (!req.getPassword().matches(PASSWORD_POLICY_REGEX)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Gyenge jelszó: minimum 8 karakter, legalább egy betű és szám.");
        }
        User user = User.builder()
                .name(req.getName())
                .email(req.getEmail())
                .password(passwordEncoder.encode(req.getPassword()))
                .role(UserRole.USER)
                .registration_date(java.time.LocalDateTime.now())
                .last_login(java.time.LocalDateTime.now())
                .is_active(true)
                .build();
        User saved = userRepository.save(user);
        log.info("auth.register.success id={}", saved.getId());
        return saved;
    }

    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElse(null);
    }

    public User login(String username, String password) {
        try {
            if (!rateLimiter.allow("login:" + username)) {
                log.warn("auth.login.rate_limited email={}", username);
                throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "Túl sok próbálkozás. Próbáld később.");
            }
            log.info("auth.login attempt email={}", username);
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, password)
            );
            org.springframework.security.core.userdetails.User userDetails =
                    (org.springframework.security.core.userdetails.User) authentication.getPrincipal();
            return userRepository.findByEmail(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found"));
        } catch (ResponseStatusException rse) {
            // Preserve explicit status codes like 429
            throw rse;
        } catch (Exception ex) {
            log.warn("auth.login.fail email={} msg={}", username, ex.getMessage());
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Hibás email vagy jelszó.");
        }
    }

    public User getCurrentUser(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Nincs bejelentkezve.");
        }
        return (User) session.getAttribute("user");
    }
}