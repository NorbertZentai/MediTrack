package hu.project.MediWeb.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
	info = @Info(
		title = "MediWeb API",
		version = "v1",
		description = "REST API for MediWeb application (authentication, medication, profile, notifications)",
		contact = @Contact(name = "MediWeb Team", email = "support@mediweb.example"),
		license = @License(name = "MIT", url = "https://opensource.org/licenses/MIT")
	),
	servers = {
		@Server(url = "http://localhost:8080", description = "Local"),
		@Server(url = "https://mediweb-backend.onrender.com", description = "Production")
	},
	security = {@SecurityRequirement(name = "bearer-jwt")}
)
@SecurityScheme(
	name = "bearer-jwt",
	type = SecuritySchemeType.HTTP,
	scheme = "bearer",
	bearerFormat = "JWT",
	description = "Provide the JWT token returned by /auth/login"
)
public class OpenApiConfig {
}
