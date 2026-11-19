package com.amillioncodelines.streamstories.support;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.postgresql.PostgreSQLContainer;

/** Provides a real PostgreSQL instance for integration tests, shared across the test suite. */
@TestConfiguration(proxyBeanMethods = false)
public class TestcontainersConfiguration {

    /** The same image as local development, read from compose.yaml, which Dependabot keeps up to date. */
    public static final String POSTGRES_IMAGE = composeImage();

    @Bean
    @ServiceConnection
    PostgreSQLContainer postgresContainer() {
        return new PostgreSQLContainer(POSTGRES_IMAGE);
    }

    private static String composeImage() {
        try {
            return Files.readAllLines(Path.of("compose.yaml")).stream()
                    .map(String::strip)
                    .filter(line -> line.startsWith("image: postgres:"))
                    .map(line -> line.substring("image: ".length()))
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException("compose.yaml has no postgres image"));
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }
}
