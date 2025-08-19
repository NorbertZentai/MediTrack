package hu.project.MediWeb.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import hu.project.MediWeb.modules.user.dto.AuthLoginRequest;
import hu.project.MediWeb.modules.user.dto.AuthRegisterRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
class AuthControllerValidationTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Test
    @DisplayName("Register validation fails -> VALIDATION_ERROR code present")
    void registerValidationError() throws Exception {
        AuthRegisterRequest req = new AuthRegisterRequest();
        // leave fields blank to trigger @NotBlank
        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.details").isArray());
    }

    @Test
    @DisplayName("Login bad credentials -> AUTH_FAILED code")
    void loginBadCredentials() throws Exception {
        AuthLoginRequest login = new AuthLoginRequest();
        login.setEmail("nincs@user.hu");
        login.setPassword("rossz");
        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("AUTH_FAILED"));
    }
}
