package hu.project.MediWeb.common.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.context.annotation.Profile;

/**
 * Test-only endpoint to trigger a RuntimeException for INTERNAL_ERROR mapping.
 * Limited to 'test' profile so it is not exposed in production.
 */
@RestController
@Profile("test")
public class TestErrorController {

    @GetMapping("/test/boom")
    public ResponseEntity<Void> boom() {
        throw new RuntimeException("boom");
    }
}
