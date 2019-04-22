package com.mg.eventbus.mq

import com.mg.eventbus.Qualifiers
import com.mg.eventbus.gateway.Commandable
import com.mg.eventbus.gateway.EveCom
import com.mg.eventbus.gateway.Fireable
import org.reflections.Reflections
import org.springframework.amqp.core.AmqpAdmin
import org.springframework.amqp.core.TopicExchange
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.context.annotation.Bean
import org.springframework.stereotype.Component

@Component(Qualifiers.COMMAND_MQ)
class CommandMQConfig(amqpAdmin: AmqpAdmin, rabbitTemplate: RabbitTemplate) : MQConfig(amqpAdmin, rabbitTemplate) {

    companion object {
        const val QUEUE_CLUSTER_ID = "Commands"
    }

    override val queueClusterId: String
        get() = QUEUE_CLUSTER_ID

    override val exchangeGatewayName: String
        get() = "EXCHANGE_COMMAND_GATEWAY"

    @Bean
    fun commandExchange() = TopicExchange(exchangeGatewayName)

    override fun getExchange() = commandExchange()

    override fun build(reflections: Reflections) {
        val classes = reflections.getSubTypesOf(Commandable::class.java)
        buildAmqp(classes)
    }

}