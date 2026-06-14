package ba.etf.fixit.notificationservice.socket;

import com.corundumstudio.socketio.Configuration;
import com.corundumstudio.socketio.SocketIOServer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;

@org.springframework.context.annotation.Configuration
public class SocketIOKonfiguracija {

    @Value("${socketio.host:0.0.0.0}")
    private String host;

    @Value("${socketio.port:9001}")
    private int port;

    @Bean
    public SocketIOServer socketIOServer() {
        Configuration config = new Configuration();
        config.setHostname(host);
        config.setPort(port);

        // null = ne provjeravaj Origin header - dozvoli sve
        config.setOrigin(null);

        config.setPingInterval(25_000);
        config.setPingTimeout(60_000);

        return new SocketIOServer(config);
    }
}