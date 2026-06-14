package ba.etf.fixit.userservice.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Provjerava da zahtjevi dolaze kroz API Gateway.
 * Ako nema X-Gateway-Secret headera - odbij sa 403.
 */
@Component
@Order(1)
public class GatewaySecurityFilter extends OncePerRequestFilter {

    @Value("${gateway.internal-secret:local-dev-secret}")
    private String gatewaySecret;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String putanja = request.getRequestURI();

        if (putanja.startsWith("/actuator") || putanja.startsWith("/swagger-ui")
                || putanja.startsWith("/api-docs")) {
            filterChain.doFilter(request, response);
            return;
        }

        String primljeniSecret = request.getHeader("X-Gateway-Secret");
        if (!gatewaySecret.equals(primljeniSecret)) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write(
                    "{\"greska\":\"FORBIDDEN\"," +
                    "\"poruka\":\"Direktan pristup mikroservisu nije dozvoljen. Koristite API Gateway.\"}");
            return;
        }

        filterChain.doFilter(request, response);
    }
}