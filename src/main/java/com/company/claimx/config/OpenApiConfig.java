package com.company.claimx.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI(){
        return new OpenAPI().info(new Info()
                .title("CLIMAX")
                .description("Expense Reimbursement and Approval Platform")
                .version("1.0.0")
        )
                // Security Setup (how to authenticate)
                // Tells Swagger: "All endpoints need Bearer token"
                .addSecurityItem(new SecurityRequirement()
                        .addList("Bearer Authentication")
                )
                // Defines what "Bearer Authentication" means
                .components(new Components()
                        .addSecuritySchemes("Bearer Authentication",createApiKeySchema())
                );
    }

    // defines JWT authentication
    private SecurityScheme createApiKeySchema() {
        return new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .bearerFormat("JWT")
                .scheme("bearer")
                .description("enter jwt token obtained form the auth/login");
    }
}
