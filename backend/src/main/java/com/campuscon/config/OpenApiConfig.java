package com.campuscon.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.Components;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

/**
 * Configuration for OpenAPI 3.0 documentation using SpringDoc.
 * Optimized for S3 hosting with proper CORS and security configuration.
 */
@Configuration
public class OpenApiConfig implements WebMvcConfigurer {

    @Value("${app.api.base-url:http://localhost:8080}")
    private String apiBaseUrl;

    @Value("${app.api.production-url:https://api.campuscon.com}")
    private String productionUrl;

    @Value("${app.docs.s3-url:https://campuscon-docs.s3.amazonaws.com}")
    private String s3DocsUrl;

    @Bean
    public OpenAPI campusConOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("CampusCon API")
                        .description("API documentation for CampusCon platform - connecting campus communities and societies. " +
                                "This documentation is hosted on AWS S3 for optimal performance and global accessibility.")
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
                                .url(apiBaseUrl)
                                .description("Development Server"),
                        new Server()
                                .url(productionUrl)
                                .description("Production Server"),
                        new Server()
                                .url(s3DocsUrl)
                                .description("S3 Documentation Server")))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("JWT token for authentication"))
                        .addSecuritySchemes("apiKey", new SecurityScheme()
                                .type(SecurityScheme.Type.APIKEY)
                                .in(SecurityScheme.In.HEADER)
                                .name("X-API-Key")
                                .description("API Key for service-to-service communication")))
                .addSecurityItem(new SecurityRequirement()
                        .addList("bearerAuth")
                        .addList("apiKey"));
    }

    /**
     * Configure CORS for OpenAPI documentation hosted on S3
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/v3/api-docs/**")
                .allowedOrigins(
                        "https://campuscon-docs.s3.amazonaws.com",
                        "https://campuscon.com",
                        "https://www.campuscon.com",
                        "http://localhost:3000",
                        "http://127.0.0.1:3000"
                )
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
                
        registry.addMapping("/swagger-ui/**")
                .allowedOrigins(
                        "https://campuscon-docs.s3.amazonaws.com",
                        "https://campuscon.com",
                        "https://www.campuscon.com",
                        "http://localhost:3000",
                        "http://127.0.0.1:3000"
                )
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }
}
