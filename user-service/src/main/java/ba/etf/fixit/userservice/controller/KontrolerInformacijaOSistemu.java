package ba.etf.fixit.userservice.controller;

import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/sistem")
public class KontrolerInformacijaOSistemu {

    @Value("${spring.application.name:user-service}")
    private String serviceName;

    @Value("${server.port:${local.server.port:0}}")
    private String serverPort;

    @Value("${eureka.instance.instance-id:}")
    private String configuredInstanceId;

    @GetMapping("/instanca-informacije")
    public ResponseEntity<Map<String, Object>> informacijeOInstanci() {
        Map<String, Object> info = new LinkedHashMap<>();
        String instanceId = (configuredInstanceId == null || configuredInstanceId.isBlank())
                ? serviceName + ":" + serverPort
                : configuredInstanceId;
        info.put("serviceName", serviceName);
        info.put("instanceId", instanceId);
        info.put("port", serverPort);
        info.put("timestamp", System.currentTimeMillis());
        return ResponseEntity.ok(info);
    }
}
