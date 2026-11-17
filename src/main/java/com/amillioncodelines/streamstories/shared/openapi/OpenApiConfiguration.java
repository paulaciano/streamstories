package com.amillioncodelines.streamstories.shared.openapi;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** General metadata of the OpenAPI specification served at {@code /v3/api-docs}. */
@Configuration(proxyBeanMethods = false)
class OpenApiConfiguration {

    @Bean
    OpenAPI streamStoriesOpenApi(@Value("${info.app.version:dev}") String version) {
        return new OpenAPI()
                .info(new Info()
                        .title("StreamStories API")
                        .description("API REST para una plataforma de catálogo audiovisual bajo demanda.")
                        .version(version)
                        .license(new License().name("MIT").url("https://opensource.org/license/mit")));
    }
}
