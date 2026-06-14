package ba.etf.fixit.notificationservice.socket;

import com.corundumstudio.socketio.HandshakeData;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.listener.ConnectListener;
import com.corundumstudio.socketio.listener.DisconnectListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Drži mapu  userId (Long) -> Set<UUID konekcija>.
 * Jedan korisnik može biti spojen na više tabova/uređaja.
 *
 * Klijent šalje userId kao query parametar pri spajanju:
 *   const socket = io("http://localhost:9001", {
 *       auth: { userId: String(user.id) }   // socket.io-client v4 šalje ovo kao query
 *   });
 *
 * NAPOMENA: netty-socketio 2.x ne podržava getAuthToken().
 * userId se čita iz URL query parametra koji socket.io-client v4
 * automatski dodaje iz auth objekta kao ?userId=...
 */
@Component
public class NotifikacijaSocketHandler {

    private static final Logger log = LoggerFactory.getLogger(NotifikacijaSocketHandler.class);

    private final SocketIOServer server;

    // userId -> skup UUID-ova aktivnih konekcija
    private final Map<Long, Set<UUID>> sesije = new ConcurrentHashMap<>();

    public NotifikacijaSocketHandler(SocketIOServer server) {
        this.server = server;
    }

    @PostConstruct
    public void registrirajListenere() {
        server.addConnectListener(onConnect());
        server.addDisconnectListener(onDisconnect());

        server.addEventListener("ping", String.class, (client, data, ack) ->
                client.sendEvent("pong", "ok"));
    }

    // --- Connect -------------------------------------------------------------

    private ConnectListener onConnect() {
        return client -> {
            String rawId = extractUserId(client.getHandshakeData());
            if (rawId == null || rawId.isBlank()) {
                log.warn("[SocketIO] Konekcija bez userId - odbačena. sessionId={}", client.getSessionId());
                client.disconnect();
                return;
            }
            try {
                Long userId = Long.parseLong(rawId.trim());
                sesije.computeIfAbsent(userId, k -> ConcurrentHashMap.newKeySet())
                      .add(client.getSessionId());
                log.info("[SocketIO] Korisnik {} spojen. sessionId={}", userId, client.getSessionId());
            } catch (NumberFormatException e) {
                log.warn("[SocketIO] Neispravan userId='{}'. Odbačeno.", rawId);
                client.disconnect();
            }
        };
    }

    // --- Disconnect -----------------------------------------------------------

    private DisconnectListener onDisconnect() {
        return client -> {
            sesije.forEach((userId, ids) -> {
                if (ids.remove(client.getSessionId())) {
                    log.info("[SocketIO] Korisnik {} odspojio se. sessionId={}", userId, client.getSessionId());
                    if (ids.isEmpty()) sesije.remove(userId);
                }
            });
        };
    }

    // --- Emitter -------------------------------------------------------------

    /**
     * Šalje event "nova-notifikacija" svim aktivnim konekcijama korisnika.
     */
    public void posaljiKorisniku(Long userId, Object payload) {
        Set<UUID> ids = sesije.get(userId);
        if (ids == null || ids.isEmpty()) {
            log.debug("[SocketIO] Korisnik {} nije online.", userId);
            return;
        }
        for (UUID sessionId : ids) {
            SocketIOClient client = server.getClient(sessionId);
            if (client != null) {
                client.sendEvent("nova-notifikacija", payload);
            }
        }
        log.info("[SocketIO] Event poslan korisniku {} ({} konekcija).", userId, ids.size());
    }

    // --- Helper: čitanje userId iz handshake-a -------------------------------

    /**
     * socket.io-client v4 s { auth: { userId: "42" } } šalje userId
     * kao URL query parametar (?userId=42).
     * Netty-socketio 2.x ga eksponira putem getSingleUrlParam().
     */
    private String extractUserId(HandshakeData data) {
        // Primarni: query param (socket.io-client v4 auth objekt)
        String fromQuery = data.getSingleUrlParam("userId");
        if (fromQuery != null && !fromQuery.isBlank()) return fromQuery;

        // Fallback: header (ako frontend šalje X-User-Id)
        String fromHeader = data.getHttpHeaders().get("X-User-Id");
        if (fromHeader != null && !fromHeader.isBlank()) return fromHeader;

        return null;
    }

    public int onlineKorisnika() {
        return (int) sesije.values().stream().filter(s -> !s.isEmpty()).count();
    }
}
