package com.example.authservice.service;

import com.example.authservice.dto.UserEmailChangedEvent;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
public class UserEventPublisher {
    private final RabbitTemplate rabbitTemplate;

    public UserEventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }


    public void publishEmailChangedEvent(Long userId, String newEmail) {
        UserEmailChangedEvent event = new UserEmailChangedEvent(userId, newEmail);
        rabbitTemplate.convertAndSend("user.exchange", "user.email.changed", event);
    }
}
