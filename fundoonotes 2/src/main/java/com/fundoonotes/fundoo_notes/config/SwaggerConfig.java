package com.fundoonotes.fundoo_notes.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Swagger / OpenAPI setup.
 * Once the app is running, open: http://localhost:8080/swagger-ui.html
 *
 * Adds an "Authorize" button in the UI so you can paste your JWT
 * (just the raw token, no need to type "Bearer ") and test every
 * protected endpoint directly from the browser.
 */
@Configuration
public class SwaggerConfig {

    private static final String SECURITY_SCHEME_NAME = "bearerAuth";

    @Bean
    public OpenAPI fundooNotesOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("FundooNotes API")
                        .description("Backend APIs for FundooNotes — Notes, Labels, " +
                                "Collaborators, Reminders, Auth (JWT + Google OAuth2)")
                        .version("v1.0"))
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME))
                .components(new Components()
                        .addSecuritySchemes(SECURITY_SCHEME_NAME, new SecurityScheme()
                                .name(SECURITY_SCHEME_NAME)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")));
    }
}
