package hu.project.MediWeb.modules.user.service;

import hu.project.MediWeb.modules.user.dto.PasswordChangeRequest;
import hu.project.MediWeb.modules.user.dto.CreateUserRequest;
import hu.project.MediWeb.modules.user.entity.User;
import hu.project.MediWeb.modules.user.enums.UserRole;
import hu.project.MediWeb.modules.user.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    private static final String PASSWORD_POLICY_REGEX = "^(?=.*[A-Za-z])(?=.*\\d).{8,}$"; // min 8, letter+digit

    public User getCurrentUser(HttpServletRequest request) {
        return (User) request.getSession().getAttribute("user");
    }

    @Transactional
    public List<User> findAllUsers() {
        return userRepository.findAll();
    }

    @Transactional
    public List<hu.project.MediWeb.modules.user.dto.UserPublicDTO> findAllUsersPublic() {
        // A LAZY LOB mező biztonságos kiolvasása még a tranzakción belül DTO-vá alakítva
        return userRepository.findAll().stream()
                .map(hu.project.MediWeb.modules.user.dto.UserPublicDTO::from)
                .toList();
    }

    @Transactional
    public Optional<User> findUserById(Long id) {
        return userRepository.findById(id);
    }

    @Transactional
    public Optional<User> findUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Transactional
    public User saveUser(User user) {
        if (user.getRegistration_date() == null) {
            user.setRegistration_date(LocalDateTime.now());
        }
        return userRepository.save(user);
    }

    @Transactional
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    @Transactional
    public User updateUserRole(Long userId, UserRole newRole) {
        return userRepository.findById(userId)
                .map(u -> {
                    u.setRole(newRole);
                    return userRepository.save(u);
                })
                .orElse(null);
    }

    @Transactional
    public boolean changePassword(User user, PasswordChangeRequest request) {
        boolean valid = passwordEncoder.matches(request.getCurrentPassword(), user.getPassword());
        boolean matchingNewPasswords = request.getNewPassword().equals(request.getReNewPassword());
        boolean notSameAsOld = !request.getCurrentPassword().equals(request.getNewPassword());

        if (valid && matchingNewPasswords && notSameAsOld) {
            String encoded = passwordEncoder.encode(request.getNewPassword());
            user.setPassword(encoded);
            userRepository.save(user);
            return true;
        } else {
            return false;
        }
    }

    @Transactional
    public void updateProfilePicture(User user, MultipartFile file) {
        try {
            byte[] bytes = file.getBytes();
            user.setProfile_picture(bytes);
            userRepository.save(user);
        } catch (IOException e) {
            throw new RuntimeException("Nem sikerült elmenteni a profilképet.", e);
        }
    }

    @Transactional
    public User createUserAdmin(CreateUserRequest req) {
        if (userRepository.findByEmail(req.getEmail()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ez az email cím már használatban van!");
        }
        if (userRepository.findByName(req.getName()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ez a felhasználónév már használatban van!");
        }
        if (!req.getPassword().matches(PASSWORD_POLICY_REGEX)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Gyenge jelszó: minimum 8 karakter, legalább egy betű és szám.");
        }
        UserRole role = UserRole.USER;
        if (req.getRole() != null && !req.getRole().isBlank()) {
            try {
                role = UserRole.valueOf(req.getRole().toUpperCase());
            } catch (IllegalArgumentException ex) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ismeretlen szerepkör: " + req.getRole());
            }
        }
        User user = User.builder()
                .name(req.getName())
                .email(req.getEmail())
                .password(passwordEncoder.encode(req.getPassword()))
                .role(role)
                .registration_date(java.time.LocalDateTime.now())
                .last_login(java.time.LocalDateTime.now())
                .is_active(true)
                .build();
        return userRepository.save(user);
    }
}
