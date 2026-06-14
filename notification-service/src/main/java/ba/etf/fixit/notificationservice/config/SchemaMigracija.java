package ba.etf.fixit.notificationservice.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Hibernate ddl-auto=update ne smanjuje/proshiruje postojece kolone.
 * Ako je tabela notifikacija kreirana ranijom verzijom enuma TipNotifikacije
 * (npr. varchar(20) ili manje), nove vrijednosti kao DODJELA_RADNIKU
 * izazivaju "Data truncated for column 'tip'".
 *
 * VAZNO: Ova migracija MORA trcati NAKON sto Hibernate zavrsi
 * ddl-auto=update (koji se izvrsava pri inicijalizaciji EntityManagerFactory,
 * tj. prilikom podizanja konteksta). @PostConstruct se izvrsava previse rano
 * (prije ili istovremeno sa Hibernate schema update-om), pa ALTER TABLE
 * moze pasti jer tabela jos ne postoji ili ce Hibernate kasnije ponovo
 * (re)kreirati kolonu sa starom duzinom.
 *
 * ApplicationReadyEvent garantuje da je cijeli Spring kontekst (uklj.
 * Hibernate schema update) zavrsen prije nego sto se ALTER TABLE izvrsi.
 */
@Component
public class SchemaMigracija {

    private static final Logger log = LoggerFactory.getLogger(SchemaMigracija.class);

    private final JdbcTemplate jdbcTemplate;

    public SchemaMigracija(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @EventListener(ApplicationReadyEvent.class)
    @Order(Integer.MAX_VALUE)
    public void migrirajKolonuTip() {
        try {
            String tip = jdbcTemplate.queryForObject(
                    "SELECT DATA_TYPE FROM INFORMATION_SCHEMA.COLUMNS " +
                    "WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'notifikacija' AND COLUMN_NAME = 'tip'",
                    String.class);
            log.info("[MIGRACIJA] Trenutni tip kolone 'notifikacija.tip' = {}", tip);

            jdbcTemplate.execute("ALTER TABLE notifikacija MODIFY COLUMN tip VARCHAR(40) NOT NULL");
            log.info("[MIGRACIJA] Kolona 'notifikacija.tip' uspjesno postavljena na VARCHAR(40).");
        } catch (Exception e) {
            log.error("[MIGRACIJA] Neuspjela migracija kolone 'tip': {}", e.getMessage());
        }
    }
}
