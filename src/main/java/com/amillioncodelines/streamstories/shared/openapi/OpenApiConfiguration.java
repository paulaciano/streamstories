package com.amillioncodelines.streamstories.shared.openapi;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * General metadata of the OpenAPI specification served at {@code /v3/api-docs}.
 *
 * <p>Every operation requires a bearer JWT by default. Public operations opt out with an empty
 * {@code @SecurityRequirements}.
 */
@Configuration(proxyBeanMethods = false)
class OpenApiConfiguration {

    static final String BEARER_SCHEME = "bearer-jwt";

    @Bean
    OpenAPI streamStoriesOpenApi(@Value("${info.app.version:dev}") String version) {
        return new OpenAPI()
                .info(new Info()
                        .title("StreamStories API")
                        .description("API REST para una plataforma de catálogo audiovisual bajo demanda.")
                        .version(version)
                        .license(new License().name("MIT").url("https://opensource.org/license/mit")))
                .components(new Components()
                        .addSecuritySchemes(
                                BEARER_SCHEME,
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("Access token obtenido en `POST /api/v1/auth/login`.")))
                .addSecurityItem(new SecurityRequirement().addList(BEARER_SCHEME));
    }
}
