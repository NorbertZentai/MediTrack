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
    public List<UserPublicDTO> getAllUsers() {
        return userService.findAllUsers().stream().map(UserPublicDTO::from).toList();
    }

    @PostMapping
    public UserPublicDTO createUser(@RequestBody User user) {
        return UserPublicDTO.from(userService.saveUser(user));
    }


    @PutMapping("/username")
    public ResponseEntity<String> updateUsername(@RequestBody String username) {
        User user = getCurrentUser();
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Nem vagy bejelentkezve.");
        }
        user.setName(username);
        userService.saveUser(user);
        return ResponseEntity.ok(username);
    }

    @PutMapping("/email")
    public ResponseEntity<String> updateEmail(@RequestBody String email) {
        User user = getCurrentUser();
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Nem vagy bejelentkezve.");
        }
        user.setEmail(email);
        userService.saveUser(user);
        return ResponseEntity.ok(email);
    }

    @PutMapping("/password")
    public ResponseEntity<?> updatePassword(@RequestBody PasswordChangeRequest requestBody) {
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
    public ResponseEntity<String> updatePhoneNumber(@RequestBody String phoneNumber) {
        User user = getCurrentUser();
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Nem vagy bejelentkezve.");
        }
        user.setPhone_number(phoneNumber);
        userService.saveUser(user);
        return ResponseEntity.ok(phoneNumber);
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
    public UserPublicDTO updateUserRole(@PathVariable Long id, @RequestParam("role") String role) {
        return UserPublicDTO.from(userService.updateUserRole(id, UserRole.valueOf(role.toUpperCase())));
    }
}