package ba.etf.fixit.reportservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Omogucava servisiranje uploadovanih fotografija kao staticki resursi.
 * Fajlovi snimljeni u `uploads/fotografije/{prijavaId}/` dostupni su
 * na putanji /uploads/fotografije/{prijavaId}/naziv.jpg
 */
@Configuration
public class WebMvcKonfiguracija implements WebMvcConfigurer {

    @Value("${fixit.upload-dir:uploads/fotografije}")
    private String uploadDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Izvuci root dio putanje (npr. "uploads" iz "uploads/fotografije")
        String rootDir = uploadDir.contains("/")
                ? uploadDir.substring(0, uploadDir.indexOf("/"))
                : uploadDir;

        registry.addResourceHandler("/" + rootDir + "/**")
                .addResourceLocations("file:" + rootDir + "/");
    }
}