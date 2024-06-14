package com.cloud.gateway.filter;

import com.cloud.gateway.config.RouteValidator;
import com.cloud.gateway.service.JwtService;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/*
 * A gateway filter that handles authentication for incoming requests.
 * It verifies the presence and validity of a JWT token for secured endpoints.
 */
@RefreshScope
@RequiredArgsConstructor
@Component
public class AuthenticationFilter implements GlobalFilter {

    private final RouteValidator routeValidator;
    private final JwtService jwtService;

    @Override
    public Mono<Void> filter(
            ServerWebExchange exchange,
            GatewayFilterChain chain
    ) {

        System.out.println("Gateway: AuthenticationFilter");

        ServerHttpRequest request = exchange.getRequest();

        // Checks if the route requires authentication
        if (routeValidator.isProtected.test(request)) {

            // If authorization header is missing, returns a 401 UNAUTHORIZED response
            if (this.isAuthMissing(request)) {
                System.out.println("Authorization header is missing");
                return this.onError(exchange, HttpStatus.UNAUTHORIZED);
            }

            try {
                // Extracts the JWT token from the request header
                final String token = this.getAuthHeader(request).substring(7);

                final String userEmail = jwtService.extractUsername(token);
                final String userRole = jwtService.extractRole(token);

                if (userEmail == null || userRole == null) {
                    System.out.println("userEmail or userRole is null");
                    return this.onError(exchange, HttpStatus.UNAUTHORIZED);
                } else {
                    // Updates the request with user information extracted from the token
                    this.updateRequest(exchange, token);

                    // If token is expired or the user's role is not authorized, returns a 403 FORBIDDEN response
                    if (jwtService.isTokenInvalid(token) ||!routeValidator.isRestricted.test(request)) {
                        System.out.println("token is invalid or user role is not authorized");
                        return this.onError(exchange, HttpStatus.FORBIDDEN);
                    }
                }

            } catch (Exception e) {
                return this.onError(exchange, HttpStatus.UNAUTHORIZED);
            }
        }

        return chain.filter(exchange);
    }

    /*
     * Sends an error response with the specified HTTP status
     */
    private Mono<Void> onError(ServerWebExchange exchange, HttpStatus httpStatus) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(httpStatus);
        return response.setComplete();
    }

    /*
     * Retrieves the Authorization header from the request
     */
    private String getAuthHeader(ServerHttpRequest request) {
        return request.getHeaders().getOrEmpty("Authorization").get(0);
    }

    /*
     * Checks if the authorization header is missing in the request
     */
    private boolean isAuthMissing(ServerHttpRequest request) {
        return !request.getHeaders().containsKey("Authorization");
    }

    /*
     * Updates the request with claims extracted from the JWT token
     */
    private void updateRequest(ServerWebExchange exchange, String token) {
        Claims claims = jwtService.getAllClaimsFromToken(token);
        exchange.getRequest().mutate()
                .header("username", claims.get("sub").toString())
                .header("role", claims.get("role").toString())
                .build();
    }
}