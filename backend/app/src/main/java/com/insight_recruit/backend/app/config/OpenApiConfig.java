package com.insight_recruit.backend.app.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI insightRecruitOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("InsightRecruit API")
                        .description("REST APIs for the AI Resume Screener engine.")
                        .version("v1.0"));
    }
}