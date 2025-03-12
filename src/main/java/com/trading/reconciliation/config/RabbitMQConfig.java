package com.trading.reconciliation.config;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ Configuration
 */
@Configuration
public class RabbitMQConfig {
    
    @Value("${reconciliation.queue.system-a}")
    private String systemAQueue;
    
    @Value("${reconciliation.queue.system-b}")
    private String systemBQueue;
    
    @Value("${reconciliation.queue.reconciliation-tasks}")
    private String reconciliationTasksQueue;
    
    @Bean
    public Queue systemAQueue() {
        return new Queue(systemAQueue, true);
    }
    
    @Bean
    public Queue systemBQueue() {
        return new Queue(systemBQueue, true);
    }
    
    @Bean
    public Queue reconciliationTasksQueue() {
        return new Queue(reconciliationTasksQueue, true);
    }
    
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
    
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        return template;
    }
} 