package hu.project.MediWeb.modules.user.controller;

import hu.project.MediWeb.modules.user.entity.User;
import hu.project.MediWeb.modules.user.enums.UserRole;
import hu.project.MediWeb.modules.user.service.UserService;
import hu.project.MediWeb.modules.user.dto.PasswordChangeRequest;
import hu.project.MediWeb.modules.user.dto.UserPublicDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import hu.project.MediWeb.modules.user.dto.UpdateUsernameRequest;
import hu.project.MediWeb.modules.user.dto.UpdateEmailRequest;
import hu.project.MediWeb.modules.user.dto.UpdatePhoneRequest;
import hu.project.MediWeb.modules.user.dto.CreateUserRequest;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated() || 
            "anonymousUser".equals(authentication.getPrincipal())) {
            return null;
        }

        String email = authentication.getName();
        Optional<User> userOptional = userService.findUserByEmail(email);
        return userOptional.orElse(null);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<UserPublicDTO> getAllUsers() {
    return userService.findAllUsersPublic();
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public UserPublicDTO createUser(@Valid @RequestBody CreateUserRequest request) {
        return UserPublicDTO.from(userService.createUserAdmin(request));
    }


    @PutMapping("/username")
    public ResponseEntity<String> updateUsername(@Valid @RequestBody UpdateUsernameRequest req) {
        User user = getCurrentUser();
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Nem vagy bejelentkezve.");
        }
        user.setName(req.getUsername());
        userService.saveUser(user);
        return ResponseEntity.ok(req.getUsername());
    }

    @PutMapping("/email")
    public ResponseEntity<String> updateEmail(@Valid @RequestBody UpdateEmailRequest req) {
        User user = getCurrentUser();
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Nem vagy bejelentkezve.");
        }
        user.setEmail(req.getEmail());
        userService.saveUser(user);
        return ResponseEntity.ok(req.getEmail());
    }

    @PutMapping("/password")
    public ResponseEntity<?> updatePassword(@Valid @RequestBody PasswordChangeRequest requestBody) {
        User user = getCurrentUser();
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Nem vagy bejelentkezve.");
        }
        boolean success = userService.changePassword(user, requestBody);

        if (success) {
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.badRequest().body("Hibás jelenlegi jelszó vagy nem egyező új jelszavak.");
        }
    }

    @PutMapping("/phone")
    public ResponseEntity<String> updatePhoneNumber(@Valid @RequestBody UpdatePhoneRequest req) {
        User user = getCurrentUser();
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Nem vagy bejelentkezve.");
        }
        user.setPhone_number(req.getPhoneNumber());
        userService.saveUser(user);
        return ResponseEntity.ok(req.getPhoneNumber());
    }

    @PutMapping("/image")
    public ResponseEntity<String> updateProfileImage(@RequestParam("file") MultipartFile file) {
        User user = getCurrentUser();
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Nem vagy bejelentkezve.");
        }
        userService.updateProfilePicture(user, file);
        return ResponseEntity.ok("Siker");
    }

    @GetMapping("/{id}")
    public UserPublicDTO getUserById(@PathVariable Long id) {
        Optional<User> user = userService.findUserById(id);
        return user.map(UserPublicDTO::from).orElse(null);
    }

    @DeleteMapping("/{id}")
    public void deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
    }

    @PutMapping("/{id}/role")
    @PreAuthorize("hasRole('ADMIN')")
    public UserPublicDTO updateUserRole(@PathVariable Long id, @RequestParam("role") String role) {
        return UserPublicDTO.from(userService.updateUserRole(id, UserRole.valueOf(role.toUpperCase())));
    }
}