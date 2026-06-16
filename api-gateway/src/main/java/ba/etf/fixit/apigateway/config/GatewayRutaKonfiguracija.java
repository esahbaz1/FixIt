package ba.etf.fixit.apigateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * Konfiguracija ruta za Spring Cloud Gateway.
 *
 * JAVNI ENDPOINTI (bez JWT-a):
 *  POST /api/auth/registracija  - kreiranje racuna
 *  POST /api/auth/prijava       - prijava, dobivanje tokena
 *  POST /api/auth/refresh       - osvjezavanje access tokena
 *  POST /api/auth/odjava        - odjava, invalidacija refresh tokena
 */
@Configuration
public class GatewayRutaKonfiguracija {

    @Bean
    public RouteLocator fixitRute(RouteLocatorBuilder builder) {
        return builder.routes()

                // USER-SERVICE (port 8081)
                .route("user-auth", r -> r
                        .path("/api/auth/**")
                        .uri("lb://user-service"))

                .route("user-korisnici", r -> r
                        .path("/api/korisnici/**")
                        .uri("lb://user-service"))

                // REPORT-SERVICE (port 8083)
                .route("report-prijave", r -> r
                        .path("/api/prijave/**")
                        .uri("lb://report-service"))

                .route("report-uploads", r -> r
                        .path("/uploads/**")
                        .uri("lb://report-service"))

                // MANAGEMENT-SERVICE (port 8082)
                .route("management-radnici", r -> r
                        .path("/api/radnici/**")
                        .uri("lb://management-service"))

                .route("management-sluzbe", r -> r
                        .path("/api/gradske-sluzbe/**")
                        .uri("lb://management-service"))

                .route("management-otkrivanje", r -> r
                        .path("/api/otkrivanje/**")
                        .uri("lb://management-service"))

                // NOTIFICATION-SERVICE (port 8084)
                .route("notification-notifikacije", r -> r
                        .path("/api/notifikacije/**")
                        .uri("lb://notification-service"))

                .build();
    }

    @Bean
    public CorsWebFilter corsWebFilter() {
        CorsConfiguration corsConfig = new CorsConfiguration();
        corsConfig.setAllowedOrigins(List.of("http://localhost:3000", "http://localhost:4200", "http://localhost:5173"));
        corsConfig.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        corsConfig.setAllowedHeaders(List.of("*"));
        corsConfig.setAllowCredentials(true);
        corsConfig.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfig);

        return new CorsWebFilter(source);
    }
}