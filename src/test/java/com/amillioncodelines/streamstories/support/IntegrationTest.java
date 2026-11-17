package com.amillioncodelines.streamstories.support;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Full application test against a real PostgreSQL. Every class using it shares the same Spring context and database,
 * so tests must not depend on a clean state: each one creates its own data (for example, users with unique emails).
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@SpringBootTest
@AutoConfigureMockMvc
@Import({TestcontainersConfiguration.class, ApiClient.class, CatalogApi.class})
@Testcontainers(disabledWithoutDocker = true)
public @interface IntegrationTest {}
