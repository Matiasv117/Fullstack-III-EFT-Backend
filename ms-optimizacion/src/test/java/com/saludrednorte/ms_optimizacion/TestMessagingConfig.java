package com.saludrednorte.ms_optimizacion;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import static org.mockito.Mockito.mock;

@TestConfiguration
public class TestMessagingConfig {

    @Bean
    @Primary
    RabbitTemplate rabbitTemplate() {
        return mock(RabbitTemplate.class);
    }
}
