package ba.etf.fixit.reportservice.saga.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * NAPOMENA: Ova konfiguracija MORA biti identicna konfiguraciji u drugom servisu
 * (isti queue argumenti), inace RabbitMQ baca PRECONDITION_FAILED kada oba servisa
 * pokusaju deklarisati isti queue sa razlicitim argumentima.
 *
 * VAZNO O QUEUE ARGUMENTIMA:
 * Queue argumenti (npr. x-dead-letter-exchange) se postavljaju SAMO prilikom
 * PRVOG kreiranja queue-a. Ako queue vec postoji u RabbitMQ-u sa drugim
 * argumentima (ili bez njih), Spring AMQP NE MOZE ih promijeniti - dobija se
 * 406 PRECONDITION_FAILED, sto izaziva beskonacnu petlju reconnect pokusaja.
 *
 * Zato OVDJE NE definisemo x-dead-letter-exchange argumente na postojecim
 * queue-ovima (q.status.promijenjen, q.notifikacija.kreirana, q.notifikacija.greska),
 * jer bi to srusilo konekciju na postojecu instalaciju.
 *
 * Beskonacna petlja iz logova je rijesena na drugi nacin:
 * factory.setDefaultRequeueRejected(false) - kada @RabbitListener baci exception,
 * poruka se NE vraca u queue (requeue=false). Bez dead-letter-exchange argumenta
 * na queue-u, RabbitMQ takvu poruku jednostavno odbacuje (DISCARD) umjesto da je
 * vrati - sto prekida petlju. Poruka se gubi, ali se vise ne ponavlja unedogled.
 *
 * Stvarni problem (Data truncated for column 'tip') rjesava SchemaMigracija,
 * koja prosiruje kolonu 'tip' na VARCHAR(40) nakon sto Hibernate zavrsi
 * ddl-auto=update (ApplicationReadyEvent).
 */
@Configuration
public class RabbitMQKonfiguracija {

    public static final String SAGA_EXCHANGE = "fixit.saga.exchange";

    public static final String STATUS_PROMIJENJEN_ROUTING_KEY    = "saga.status.promijenjen";
    public static final String NOTIFIKACIJA_KREIRANA_ROUTING_KEY = "saga.notifikacija.kreirana";
    public static final String NOTIFIKACIJA_GRESKA_ROUTING_KEY   = "saga.notifikacija.greska";

    public static final String STATUS_PROMIJENJEN_QUEUE    = "q.status.promijenjen";
    public static final String NOTIFIKACIJA_KREIRANA_QUEUE = "q.notifikacija.kreirana";
    public static final String NOTIFIKACIJA_GRESKA_QUEUE   = "q.notifikacija.greska";

    @Bean
    public TopicExchange sagaExchange() {
        return new TopicExchange(SAGA_EXCHANGE, true, false);
    }

    @Bean
    public Queue statusPromijenjenQueue() {
        return QueueBuilder.durable(STATUS_PROMIJENJEN_QUEUE).build();
    }

    @Bean
    public Queue notifikacijaKreiranaQueue() {
        return QueueBuilder.durable(NOTIFIKACIJA_KREIRANA_QUEUE).build();
    }

    @Bean
    public Queue notifikacijaGreskaQueue() {
        return QueueBuilder.durable(NOTIFIKACIJA_GRESKA_QUEUE).build();
    }

    @Bean
    public Binding bindingStatusPromijenjen() {
        return BindingBuilder.bind(statusPromijenjenQueue())
                .to(sagaExchange())
                .with(STATUS_PROMIJENJEN_ROUTING_KEY);
    }

    @Bean
    public Binding bindingNotifikacijaKreirana() {
        return BindingBuilder.bind(notifikacijaKreiranaQueue())
                .to(sagaExchange())
                .with(NOTIFIKACIJA_KREIRANA_ROUTING_KEY);
    }

    @Bean
    public Binding bindingNotifikacijaGreska() {
        return BindingBuilder.bind(notifikacijaGreskaQueue())
                .to(sagaExchange())
                .with(NOTIFIKACIJA_GRESKA_ROUTING_KEY);
    }

    @Bean
    public MessageConverter jacksonMessageConverter() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return new Jackson2JsonMessageConverter(objectMapper);
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jacksonMessageConverter());
        return template;
    }

    /**
     * Listener container factory koja NE vraca poruku u queue
     * ako @RabbitListener baci exception (setDefaultRequeueRejected(false)).
     * Bez ovoga, svaka neuspjela poruka se vraca natrag u queue i odmah
     * ponovo obradjuje - beskonacna petlja.
     */
    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(jacksonMessageConverter());
        factory.setDefaultRequeueRejected(false);
        return factory;
    }
}
