package com.mg.eventbus.producer

import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.stereotype.Component

abstract class MessageProducer(val rabbitTemplate: RabbitTemplate) {

    protected fun <T> sendMessage(eventData: EventData<T>) {
        rabbitTemplate.convertAndSend(
                eventData.topic,
                eventData.routingKey,
                eventData.data
        )

    }

}