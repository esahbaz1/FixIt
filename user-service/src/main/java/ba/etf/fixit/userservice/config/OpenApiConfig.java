package ba.etf.fixit.userservice.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("FixIt - User Service API")
                        .description("Mikroservis za upravljanje korisnicima: registracija, autentifikacija i profil korisnika.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("ETF Sarajevo - NWT")
                                .email("nwt@etf.unsa.ba")))
                .servers(List.of(
                        new Server().url("http://localhost:8081").description("Lokalni development server")));
    }
}
