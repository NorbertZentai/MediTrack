package hu.project.MediWeb.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import hu.project.MediWeb.modules.user.dto.AuthLoginRequest;
import hu.project.MediWeb.modules.user.dto.AuthRegisterRequest;
import hu.project.MediWeb.modules.user.entity.User;
import hu.project.MediWeb.modules.user.enums.UserRole;
import hu.project.MediWeb.modules.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
class SecurityAuthorizationTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired UserRepository userRepository;
    @Autowired PasswordEncoder passwordEncoder;

    @BeforeEach
    void setup() {
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("Anonymous cannot access /api/secure/ping and receives 401")
    void anonymousSecureDenied() throws Exception {
        mockMvc.perform(get("/api/secure/ping"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value("AUTH_FAILED"))
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Unauthorized"));
    }

    @Test
    @DisplayName("User role cannot access /api/admin/status but admin can")
    void adminRestriction() throws Exception {
        User admin = User.builder()
                .name("AdminUser")
                .email("admin@example.hu")
                .password(passwordEncoder.encode("AdminPass1"))
                .role(UserRole.ADMIN)
                .registration_date(LocalDateTime.now())
                .last_login(LocalDateTime.now())
                .is_active(true)
                .build();
        userRepository.save(admin);

        AuthRegisterRequest reg = new AuthRegisterRequest();
        reg.setName("Normal"); reg.setEmail("normal@example.hu"); reg.setPassword("Passw0rd1");
        mockMvc.perform(post("/auth/register").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reg)))
                .andExpect(status().isOk());

        AuthLoginRequest loginUser = new AuthLoginRequest();
        loginUser.setEmail("normal@example.hu"); loginUser.setPassword("Passw0rd1");
        String userToken = objectMapper.readTree(mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginUser)))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString()).get("token").asText();

        mockMvc.perform(get("/api/admin/status").header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value("FORBIDDEN"))
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.error").value("Forbidden"));

        AuthLoginRequest loginAdmin = new AuthLoginRequest();
        loginAdmin.setEmail("admin@example.hu"); loginAdmin.setPassword("AdminPass1");
        String adminToken = objectMapper.readTree(mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginAdmin)))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString()).get("token").asText();

        mockMvc.perform(get("/api/admin/status").header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(content().string("admin-ok"));
    }

    @Test
    @DisplayName("Invalid token -> 401 for secure endpoint")
    void invalidToken() throws Exception {
        mockMvc.perform(get("/api/secure/ping").header("Authorization", "Bearer invalid.token.value"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value("AUTH_FAILED"))
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Unauthorized"));
    }
}
