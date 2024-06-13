# Spring Cloud Gateway with JWT

El código consiste en usar un filtro (`AuthenticationFilter`) para verificar todas las solicitudes entrantes a la API. Si la solicitud es a un recurso protegido, entonces el filtro verifica la validez del JWT. Si el token es válido y el usuario tiene privilegio suficiente, la solicitud se enruta al microservicio relevante.

## Credits

* Rajith Delantha, [Spring Cloud Gateway security with JWT](https://medium.com/@rajithgama/spring-cloud-gateway-security-with-jwt-23045ba59b8a)