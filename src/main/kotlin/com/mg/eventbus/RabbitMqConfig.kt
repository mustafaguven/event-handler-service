package com.mg.eventbus

import org.springframework.amqp.core.AmqpAdmin
import org.springframework.amqp.rabbit.connection.ConnectionFactory
import org.springframework.amqp.rabbit.core.RabbitAdmin
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.amqp.rabbit.listener.RabbitListenerEndpointRegistry
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter
import org.springframework.context.annotation.Bean


abstract class RabbitMqConfig {

    @Bean
    open fun messageConverter() = Jackson2JsonMessageConverter()

    @Bean
    open fun rabbitTemplate(connectionFactory: ConnectionFactory): RabbitTemplate {
        val rabbitTemplate = RabbitTemplate(connectionFactory)
        rabbitTemplate.messageConverter = messageConverter()
        return rabbitTemplate
    }

    @Bean
    open fun amqpAdmin(connectionFactory: ConnectionFactory): AmqpAdmin {
        return RabbitAdmin(connectionFactory)
    }

    @Bean
    open fun registry(): RabbitListenerEndpointRegistry {
        return RabbitListenerEndpointRegistry()
    }

}