package com.mg.eventbus

import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.amqp.rabbit.connection.ConnectionFactory
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration


abstract class RabbitMqConfig {

    companion object {
        const val QUEUE_ORDER_ALL = "order.all"
    }


    @Bean
    open fun messageConverter() = Jackson2JsonMessageConverter()

    @Bean
    open fun rabbitTemplate(connectionFactory: ConnectionFactory): RabbitTemplate {
        val rabbitTemplate = RabbitTemplate(connectionFactory)
        rabbitTemplate.messageConverter = messageConverter()
        return rabbitTemplate
    }

    @RabbitListener(queues = [QUEUE_ORDER_ALL])
    fun <T> consumeOrderAll(message: T) {
        println(message)
    }

}