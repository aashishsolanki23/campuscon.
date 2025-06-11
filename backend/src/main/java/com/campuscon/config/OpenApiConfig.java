package com.campuscon.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Configuration for OpenAPI 3.0 documentation using SpringDoc.
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI campusConOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("CampusCon API")
                        .description("API documentation for CampusCon platform - connecting campus communities and societies")
                        .version("v1.0.0")
                        .contact(new Contact()
                                .name("CampusCon Support")
                                .email("support@campuscon.com")
                                .url("https://campuscon.com/support"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8080")
                                .description("Development Server"),
                        new Server()
                                .url("https://api.campuscon.com")
                                .description("Production Server")));
    }
}
