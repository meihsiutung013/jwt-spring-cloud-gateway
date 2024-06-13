package com.cloud.gateway.config;

import com.cloud.gateway.filter.AuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class GatewayConfig {

    private final AuthenticationFilter filter;

    @Bean
    public RouteLocator routes(RouteLocatorBuilder builder) {
        return builder.routes()

                .route("microservice-users", r -> r
                        .path("/api/users/**")
                        .filters(f -> f.filter(filter))
                        .uri("lb://microservice-users"))
                .route("microservice-users", r -> r
                        .path("/api/sessions/**")
                        .filters(f -> f.filter(filter))
                        .uri("lb://microservice-users"))

                .route("microservice-tests", r -> r
                        .path("/api/tests/**")
                        .filters(f -> f.filter(filter))
                        .uri("lb://microservice-tests"))

                .build();
    }

}