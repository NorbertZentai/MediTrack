package hu.project.MediWeb.rate;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class RateLimitAdminControllerIT {

    @Autowired
    MockMvc mockMvc;

    @Test
    void unauthenticatedCannotAccessAdminEndpoint() throws Exception {
        mockMvc.perform(get("/api/admin/rate-limit"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void patchWithoutAuthRejected() throws Exception {
        mockMvc.perform(patch("/api/admin/rate-limit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"limit\":5}"))
                .andExpect(status().isUnauthorized());
    }
}
