package ba.etf.fixit.managementservice.service;

import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class ServisZaOtkrivanje {

    private final RestTemplate directRestTemplate;
    private final RestTemplate loadBalancedRestTemplate;
    private final String directBaseUrl;

    public ServisZaOtkrivanje(
            @Qualifier("directRestTemplate") RestTemplate directRestTemplate,
            @Qualifier("loadBalancedRestTemplate") RestTemplate loadBalancedRestTemplate,
            @Value("${discovery.user-service.direct-base-url}") String directBaseUrl) {
        this.directRestTemplate = directRestTemplate;
        this.loadBalancedRestTemplate = loadBalancedRestTemplate;
        this.directBaseUrl = directBaseUrl;
    }

    public Map<String, Object> direktnaProvjera() {
        String url = directBaseUrl + "/api/sistem/instanca-informacije";
        return probe("DIRECT", url, directRestTemplate);
    }

    public Map<String, Object> balansiranaProvjera() {
        String url = "http://USER-SERVICE/api/sistem/instanca-informacije";
        return probe("LOAD_BALANCED", url, loadBalancedRestTemplate);
    }

    private Map<String, Object> probe(String mode, String targetUrl, RestTemplate restTemplate) {
        long start = System.nanoTime();
        Map<?, ?> userServiceInfo = restTemplate.getForObject(targetUrl, Map.class);
        long durationMs = (System.nanoTime() - start) / 1_000_000;

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("mode", mode);
        response.put("targetUrl", targetUrl);
        response.put("durationMs", durationMs);
        response.put("userServiceInstance", userServiceInfo);
        return response;
    }
}
