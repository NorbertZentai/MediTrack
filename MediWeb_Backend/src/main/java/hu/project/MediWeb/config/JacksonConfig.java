package hu.project.MediWeb.config;

import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.datatype.hibernate6.Hibernate6Module;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JacksonConfig {

    @Bean
    public Module hibernateModule() {
        Hibernate6Module module = new Hibernate6Module();
        // Ne próbálja meg bekényszeríteni a lazy betöltést szerializációkor
        module.configure(Hibernate6Module.Feature.FORCE_LAZY_LOADING, false);
        // Ignore transient field errors
        return module;
    }
}
