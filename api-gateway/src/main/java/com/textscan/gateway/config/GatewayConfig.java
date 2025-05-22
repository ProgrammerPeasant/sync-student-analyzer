package com.textscan.gateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayConfig {

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("file-storage-service-api", r -> r
                        .path("/api/files/**")
                        .uri("lb://file-storage-service"))
                .route("file-storage-service-openapi", r -> r
                        .path("/v3/api-docs/file-storage-service")
                        .filters(f -> f.rewritePath("/v3/api-docs/file-storage-service", "/v3/api-docs"))
                        .uri("lb://file-storage-service"))

                .route("file-analysis-service-api", r -> r
                        .path("/api/analysis/**")
                        .uri("lb://file-analysis-service"))
                .route("file-analysis-service-openapi", r -> r
                        .path("/v3/api-docs/file-analysis-service")
                        .filters(f -> f.rewritePath("/v3/api-docs/file-analysis-service", "/v3/api-docs"))
                        .uri("lb://file-analysis-service"))
                .build();
    }
}