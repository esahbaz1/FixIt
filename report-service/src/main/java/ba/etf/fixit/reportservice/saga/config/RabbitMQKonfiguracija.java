package ba.etf.fixit.reportservice.saga.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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
}