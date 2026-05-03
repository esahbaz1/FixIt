package ba.etf.fixit.managementservice.security;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Registruje UlogaInterceptor za sve puteve u management-servisu.
 */
@Configuration
public class SecurityKonfiguracija implements WebMvcConfigurer {

    private final UlogaInterceptor ulogaInterceptor;

    public SecurityKonfiguracija(UlogaInterceptor ulogaInterceptor) {
        this.ulogaInterceptor = ulogaInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(ulogaInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns("/actuator/**", "/swagger-ui/**", "/api-docs/**");
    }
}
