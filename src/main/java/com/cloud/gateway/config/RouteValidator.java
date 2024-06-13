package com.cloud.gateway.config;

import com.cloud.gateway.model.Endpoint;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

/*
 * This class checks if a given request should be secured or not,
 * based on predefined open API endpoints
 */
@Component
public class RouteValidator {

    /*
     * Endpoints that don't require authentication
     */
    public static final Map<String, String> noAuthEndpoints = Map.of(
            // microservice-users
            "/api/sessions", "POST",
            "/api/users", "POST",

            // microservice-tests
            "/api/tests/all", "GET"
    );

    /*
     * Endpoints that require authentication
     */
    public static final List<Endpoint> authEndpoints = List.of(
            // microservice-users

            // microservice-tests
            new Endpoint("/api/tests/user", "GET", "USER"),
            new Endpoint("/api/tests/admin", "GET", "ADMIN")
    );

    /*
     * This predicate checks if a request needs to be authenticated
     * by verifying if the request path and method are not in the
     * noAuthEndpoints map.
     */
    public Predicate<ServerHttpRequest> isProtected =
        request -> noAuthEndpoints
            .entrySet()
            .stream()
            .noneMatch(entry -> request.getURI().getPath().contains(entry.getKey())
                && (entry.getValue().equals("*") || request.getMethod().name().equalsIgnoreCase(entry.getValue()))
            );

    /*
     * This predicate checks if a request needs a specific role
     * to be authorized by verifying if the request path, method
     * and role are in the authEndpoints list.
     */
    public Predicate<ServerHttpRequest> isRestricted =
            request -> authEndpoints
                    .stream()
                    .anyMatch(endpoint -> request.getURI().getPath().contains(endpoint.getPath())
                            && (endpoint.getMethod().equals("*")
                                || request.getMethod().name().equalsIgnoreCase(endpoint.getMethod()))
                            && (request.getHeaders().getOrEmpty("role").get(0).equals("ADMIN")
                                || request.getHeaders().getOrEmpty("role").get(0).equals(endpoint.getRole()))
                    );

}