package ba.etf.fixit.notificationservice.socket;

import com.corundumstudio.socketio.SocketIOServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.SmartLifecycle;
import org.springframework.stereotype.Component;

/**
 * Pokreće / zaustavlja SocketIOServer zajedno sa Spring kontekstom.
 * SmartLifecycle se koristi umjesto @PostConstruct/@PreDestroy
 * kako bi se server zaustavio ispred Beana koji ga koriste.
 */
@Component
public class SocketIOLifecycle implements SmartLifecycle {

    private static final Logger log = LoggerFactory.getLogger(SocketIOLifecycle.class);

    private final SocketIOServer server;
    private volatile boolean running = false;

    public SocketIOLifecycle(SocketIOServer server) {
        this.server = server;
    }

    @Override
    public void start() {
        server.start();
        running = true;
        log.info("[SocketIO] Server pokrenut na portu {}", server.getConfiguration().getPort());
    }

    @Override
    public void stop() {
        server.stop();
        running = false;
        log.info("[SocketIO] Server zaustavljen.");
    }

    @Override public boolean isRunning()   { return running; }
    @Override public int    getPhase()     { return Integer.MAX_VALUE; }
    @Override public boolean isAutoStartup(){ return true; }
}
