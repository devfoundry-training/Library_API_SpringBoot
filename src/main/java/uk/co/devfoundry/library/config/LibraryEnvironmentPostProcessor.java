package uk.co.devfoundry.library.config;

import java.util.Map;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

/**
 * Reads DATABASE_URL and TEST_MODE before Spring builds anything, so the DataSource, Flyway and
 * Hibernate all see the same database. Registered in META-INF/spring.factories.
 */
public class LibraryEnvironmentPostProcessor implements EnvironmentPostProcessor {

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        DatabaseUrl database = DatabaseUrl.parse(environment.getProperty("DATABASE_URL"));
        Map<String, Object> properties = database.springProperties();
        // Only exactly "1" turns test mode on.
        properties.put("library.test-mode", String.valueOf("1".equals(environment.getProperty("TEST_MODE"))));
        environment.getPropertySources().addFirst(new MapPropertySource("libraryEnvironment", properties));
    }
}
