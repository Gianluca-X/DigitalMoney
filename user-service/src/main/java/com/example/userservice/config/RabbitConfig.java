package com.example.userservice.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    // Declaramos el exchange (mismo nombre que usa auth-service)
    @Bean
    public TopicExchange userExchange() {
        return new TopicExchange("user.exchange", true, false);
    }

    // Cola para cambio de email
    @Bean
    public Queue userEmailChangedQueue() {
        return new Queue("user.email.changed", true);
    }

    // Cola para registro de usuario
    @Bean
    public Queue userRegisterQueue() {
        return new Queue("user.register.queue", true);
    }

    // Binding de cola de cambio de email con su routingKey
    @Bean
    public Binding bindingUserEmailChanged(Queue userEmailChangedQueue, TopicExchange userExchange) {
        return BindingBuilder.bind(userEmailChangedQueue)
                .to(userExchange)
                .with("user.email.changed");
    }

    // Binding de cola de registro con su routingKey
    @Bean
    public Binding bindingUserRegister(Queue userRegisterQueue, TopicExchange userExchange) {
        return BindingBuilder.bind(userRegisterQueue)
                .to(userExchange)
                .with("user.register.queue");
    }
}
