package hu.project.MediWeb.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import hu.project.MediWeb.modules.user.dto.AuthLoginRequest;
import hu.project.MediWeb.modules.user.dto.AuthRegisterRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
class AuthPositiveFlowTest {

    @Autowired
    MockMvc mockMvc;
    @Autowired
    ObjectMapper objectMapper;

    @Test
    @DisplayName("Register -> Login -> /auth/me returns current user")
    void registerLoginMe() throws Exception {
        AuthRegisterRequest reg = new AuthRegisterRequest();
        reg.setName("Teszt Elek");
        reg.setEmail("teszt@example.hu");
        reg.setPassword("Passw0rd1");
        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reg)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("teszt@example.hu"));

        AuthLoginRequest login = new AuthLoginRequest();
        login.setEmail("teszt@example.hu");
        login.setPassword("Passw0rd1");
        MvcResult loginResult = mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isString())
                .andReturn();

        String body = loginResult.getResponse().getContentAsString();
        String token = objectMapper.readTree(body).get("token").asText();
        assertThat(token).isNotBlank();

        mockMvc.perform(get("/auth/me").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("teszt@example.hu"))
                .andExpect(jsonPath("$.name").value("Teszt Elek"));
    }
}
