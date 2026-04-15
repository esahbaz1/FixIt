package ba.etf.fixit.reportservice.config;

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
                        .title("FixIt - Report Service API")
                        .description("Mikroservis za upravljanje prijavama komunalnih problema. Pokriva kreiranje i praćenje prijava, komentare, fotografije, historiju statusa i validacije.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("ETF Sarajevo - NWT")
                                .email("nwt@etf.unsa.ba")))
                .servers(List.of(
                        new Server().url("http://localhost:8083").description("Lokalni development server")));
    }
}
