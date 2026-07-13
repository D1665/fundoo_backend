package com.fundoonotes.fundoo_notes.jms;

import com.fundoonotes.fundoo_notes.config.RabbitMQConfig;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ReminderProducer {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    public void sendReminder(String email, String noteTitle) {
        String message = email + "|" + noteTitle;

        rabbitTemplate.convertAndSend(
                RabbitMQConfig.REMINDER_EXCHANGE,
                RabbitMQConfig.REMINDER_ROUTING_KEY,
                message
        );

        System.out.println("Reminder sent to RabbitMQ: " + message);
    }
}