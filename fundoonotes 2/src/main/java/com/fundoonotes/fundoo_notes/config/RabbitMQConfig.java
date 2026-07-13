package com.fundoonotes.fundoo_notes.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String REMINDER_QUEUE = "reminder-queue";
    public static final String REMINDER_EXCHANGE = "reminder-exchange";
    public static final String REMINDER_ROUTING_KEY = "reminder-key";

    @Bean
    public Queue reminderQueue() {
        return new Queue(REMINDER_QUEUE, true);
    }

    @Bean
    public DirectExchange reminderExchange() {
        return new DirectExchange(REMINDER_EXCHANGE);
    }

    @Bean
    public Binding reminderBinding(Queue reminderQueue, DirectExchange reminderExchange) {
        return BindingBuilder
                .bind(reminderQueue)
                .to(reminderExchange)
                .with(REMINDER_ROUTING_KEY);
    }

    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter());
        return template;
    }
}