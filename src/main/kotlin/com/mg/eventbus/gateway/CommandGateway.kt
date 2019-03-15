package com.mg.eventbus.gateway

import com.mg.eventbus.inline.logger
import lombok.extern.slf4j.Slf4j
import org.reflections.Reflections
import org.springframework.amqp.core.*
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.context.annotation.Bean
import org.springframework.stereotype.Component
import javax.annotation.PreDestroy


@Slf4j
@Component
class CommandGateway(private val rabbitTemplate: RabbitTemplate,
                     val amqpAdmin: AmqpAdmin) {

    private val declaredQueues: HashSet<Queue> by lazy { HashSet<Queue>() }
    private val declaredBindings: HashSet<Binding> by lazy { HashSet<Binding>() }

    companion object {
        val log = logger(this)
        const val COMMAND_GATEWAY_EXCHANGE = "COMMAND_GATEWAY_EXCHANGE"
    }

    @Bean
    fun commandGatewayExchange() = DirectExchange(COMMAND_GATEWAY_EXCHANGE)

    fun onApplicationReadyEvent(packageName: String) {
        val reflections = Reflections(packageName)
        val classes = reflections.getSubTypesOf(Commandable::class.java)
        classes.forEach {
            val queueName = it.simpleName
            if (amqpAdmin.getQueueProperties(queueName) == null) {
                val queue = Queue(queueName)
                val binding = BindingBuilder.bind(queue).to(commandGatewayExchange()).with(queueName)
                amqpAdmin.declareQueue(queue)
                amqpAdmin.declareBinding(binding)
                declaredQueues.add(queue)
                declaredBindings.add(binding)
                log.info("$queueName named queue is created on RabbitMq successfully")
            }
        }
    }

    fun <T : Commandable> send(t: T) {
        rabbitTemplate.convertAndSend(COMMAND_GATEWAY_EXCHANGE, t.javaClass.simpleName, t)
        log.info("message sent to ${t.javaClass.simpleName} successfully")
    }


    @PreDestroy
    fun onDestroy() {
        declaredBindings.forEach {
            try {
                amqpAdmin.removeBinding(it)
                log.info("${it.routingKey} named binding is deleted from RabbitMq successfully")
            } catch (ex: Exception) {
                log.error(ex.message)
            }
        }

        declaredQueues.forEach {
            try {
                amqpAdmin.deleteQueue(it.name)
                log.info("${it.name} named queue is deleted from RabbitMq successfully")
            } catch (ex: Exception) {
                log.error(ex.message)
            }
        }

        amqpAdmin.deleteExchange(COMMAND_GATEWAY_EXCHANGE)
        log.info("$COMMAND_GATEWAY_EXCHANGE named exchange is deleted from RabbitMq successfully")

        declaredQueues.clear()
        declaredBindings.clear()
    }

}