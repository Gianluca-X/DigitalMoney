package com.example.userservice.listener;

import com.example.authservice.dto.UserEmailChangedEvent;
import com.example.userservice.dto.entry.UserRegisterRequest;
import com.example.userservice.service.impl.UserServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserEventListener {

    private final UserServiceImpl userService;

    // Evento para actualizar email
    @RabbitListener(queues = "user.email.changed")
    public void handleEmailChangedEvent(UserEmailChangedEvent event) {
        userService.updateUserEmail(event.getUserId(), event.getNewEmail());
        log.info("Email actualizado desde evento: {}", event.getNewEmail());
    }

    // Evento para registrar usuario
    @RabbitListener(queues = "user.register.queue")
    public void handleUserRegisterEvent(UserRegisterRequest event) {
        userService.createUserFromEvent(event);
        log.info("Usuario creado desde evento: {}", event.getEmail());
    }
}
