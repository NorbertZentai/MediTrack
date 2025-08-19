package hu.project.MediWeb.web;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/secure")
public class SecureController {

    @GetMapping("/ping")
    public ResponseEntity<String> ping(Authentication auth) {
        return ResponseEntity.ok("secure-pong:" + (auth != null ? auth.getName() : "?"));
    }
}
