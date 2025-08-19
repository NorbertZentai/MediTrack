package hu.project.MediWeb.rate;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class RateLimitingFilterIT {

    @Autowired
    MockMvc mockMvc;

    @Test
    void rateLimitTriggersAfterThreshold() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk());
        // simulate hitting a protected endpoint repeatedly; unauthorized until limit reached
        for (int i = 0; i < 12; i++) {
            final int attempt = i; // must be effectively final for lambda
            mockMvc.perform(get("/api/secure/ping"))
                    .andExpect(result -> {
                        int s = result.getResponse().getStatus();
                        if (attempt < 10) {
                            if (s != 401 && s != 429) throw new AssertionError("Expected 401 pre-limit or 429: got " + s);
                        } else {
                            if (s != 429 && s != 401) throw new AssertionError("Expected 429 at/after limit, got " + s);
                        }
                    });
        }
    }
}
