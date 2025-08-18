package hu.project.MediWeb.integration;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.containers.PostgreSQLContainer;

@SpringBootTest
@Testcontainers
class DatabaseSmokeTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
            .withDatabaseName("meditrack_test")
            .withUsername("postgres")
            .withPassword("postgres");

    @DynamicPropertySource
    static void registerProps(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.sql.init.mode", () -> "never");
    }

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Test
    void contextLoads_andFlywayApplied() {
        Integer userTableExists = jdbcTemplate.queryForObject(
                "SELECT count(*) FROM information_schema.tables WHERE table_name='users'", Integer.class);
        Assertions.assertNotNull(userTableExists);
        Assertions.assertEquals(1, userTableExists, "users table should exist after Flyway migration");
    }
}
