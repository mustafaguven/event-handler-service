package com.mg.eventbus.mq

import com.mg.eventbus.Qualifiers
import org.springframework.amqp.core.AmqpAdmin
import org.springframework.amqp.core.TopicExchange
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.context.annotation.Bean
import org.springframework.stereotype.Component

@Component(Qualifiers.EVENT_MQ)
class EventMQConfig(amqpAdmin: AmqpAdmin, rabbitTemplate: RabbitTemplate) : MQConfig(amqpAdmin, rabbitTemplate) {

    companion object {
        const val QUEUE_CLUSTER_ID = "Events"
    }

    override val queueClusterId: String
        get() = QUEUE_CLUSTER_ID

    override val exchangeGatewayName: String
        get() = "EXCHANGE_EVENT_GATEWAY"

    @Bean
    fun eventExchange() = TopicExchange(exchangeGatewayName)

    override fun getExchange() = eventExchange()

}