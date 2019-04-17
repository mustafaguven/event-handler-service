package com.mg.eventbus.gateway

import com.mg.eventbus.cache.LRUCache
import com.mg.eventbus.exception.CommandTimeoutException
import com.mg.eventbus.inline.logger
import com.mg.eventbus.response.BaseResponse
import lombok.extern.slf4j.Slf4j
import org.reflections.Reflections
import org.springframework.amqp.core.*
import org.springframework.amqp.core.Queue
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.context.annotation.Bean
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Component
import java.util.*
import java.util.concurrent.CompletableFuture
import javax.annotation.PreDestroy


@Slf4j
@Component
class EveCom(private val rabbitTemplate: RabbitTemplate,
             val amqpAdmin: AmqpAdmin) {

    private val commandCache = LRUCache<Any>()
    private val declaredQueues: HashSet<Queue> by lazy { HashSet<Queue>() }
    private val declaredBindings: HashSet<Binding> by lazy { HashSet<Binding>() }

    companion object {
        private const val INTERVAL = 50L
        private const val MAX_TRYING = 100
        val log = logger(this)
        const val EXCHANGE_COMMAND_GATEWAY = "EXCHANGE_COMMAND_GATEWAY"
        const val EXCHANGE_EVENT_GATEWAY = "EXCHANGE_EVENT_GATEWAY"
        const val QUEUE_COMMAND_CLUSTER_ID: String = "Commands"
        const val QUEUE_EVENT_CLUSTER_ID: String = "Events"
        const val QUEUE_COMMAND_CLUSTER_ROUTE_KEY = QUEUE_COMMAND_CLUSTER_ID.plus(".*")
        const val QUEUE_EVENT_CLUSTER_ROUTE_KEY = QUEUE_EVENT_CLUSTER_ID.plus(".*")
    }

    @Bean
    fun commandExchange() = TopicExchange(EXCHANGE_COMMAND_GATEWAY)

    @Bean
    fun eventExchange() = TopicExchange(EXCHANGE_EVENT_GATEWAY)

    fun prepareQueueName(type: String, simpleName: String) = type.plus(".").plus(simpleName)

    fun onApplicationReadyEvent(packageName: String) {
        val reflections = Reflections(packageName)
        prepareMQForCommand(reflections)
        prepareMQForEvent(reflections)
    }

    private fun prepareMQForCommand(reflections: Reflections) {
        val classes = reflections.getSubTypesOf(Commandable::class.java)
        createQueue(QUEUE_COMMAND_CLUSTER_ID, QUEUE_COMMAND_CLUSTER_ROUTE_KEY) { commandExchange() }
        classes.forEach {
            val queueName = prepareQueueName(QUEUE_COMMAND_CLUSTER_ID, it.simpleName)
            createQueue(queueName) {
                commandExchange()
            }
        }
    }

    private fun prepareMQForEvent(reflections: Reflections) {
        val classes = reflections.getSubTypesOf(Eventable::class.java)
        createQueue(QUEUE_EVENT_CLUSTER_ID, QUEUE_EVENT_CLUSTER_ROUTE_KEY) { eventExchange() }
        classes.forEach {
            val queueName = prepareQueueName(QUEUE_EVENT_CLUSTER_ID, it.simpleName)
            createQueue(queueName) {
                eventExchange()
            }
        }
    }

    private fun createQueue(queueName: String, bindingName: String = queueName, exchangeFunc: () -> TopicExchange) {
        if (amqpAdmin.getQueueProperties(queueName) == null) {
            val queue = Queue(queueName)
            val binding = BindingBuilder.bind(queue).to(exchangeFunc()).with(bindingName)
            amqpAdmin.declareQueue(queue)
            amqpAdmin.declareBinding(binding)
/*            declaredQueues.add(queue)
            declaredBindings.add(binding)*/
            log.info("$queueName named queue is created on RabbitMq successfully")
            log.info("$queueName named routing key is created on RabbitMq successfully")
        }

    }

    private fun <T : Commandable> convertAndSendCommand(t: T) {
        val queueName = prepareQueueName(QUEUE_COMMAND_CLUSTER_ID, t.javaClass.simpleName)
        rabbitTemplate.convertAndSend(EXCHANGE_COMMAND_GATEWAY, queueName, t)
        log.info("command sent to $queueName successfully")
    }

    private fun <T : Eventable> convertAndSendEvent(t: T) {
        val queueName = prepareQueueName(QUEUE_EVENT_CLUSTER_ID, t.javaClass.simpleName)
        rabbitTemplate.convertAndSend(EXCHANGE_EVENT_GATEWAY, queueName, t)
        log.info("event sent to $queueName successfully")
    }

    fun onHandle(command: Commandable, func: () -> Any) {
        if (command.entity != null) {
            commandCache[command.uuid] = func()
        }
    }

    fun sendCommand(command: Commandable) = CompletableFuture.supplyAsync<ResponseEntity<BaseResponse>> {
        convertAndSendCommand(command)
        var trying = 0
        while (true) {
            trying++
            if (isCommandDone(command)) {
                break
            }
            if (trying > MAX_TRYING) {
                return@supplyAsync returnFailResponse(CommandTimeoutException(command, INTERVAL * MAX_TRYING))
            }
            Thread.sleep(INTERVAL)
        }
        val result = commandCache[command.uuid]
        return@supplyAsync if (result is Throwable) returnFailResponse(result) else returnSuccessResponse(command)
    }

    fun publishEvent(event: Eventable) {
        convertAndSendEvent(event)
    }

    private fun returnSuccessResponse(command: Commandable): ResponseEntity<BaseResponse> {
        val response = BaseResponse()
        response.status = 1
        response.data = commandCache[command.uuid]
        return ResponseEntity.ok(response)
    }

    private fun returnFailResponse(ex: Throwable): ResponseEntity<BaseResponse> {
        val response = BaseResponse()
        response.status = 0
        response.message = ex.message
        return ResponseEntity.ok(response)
    }

    private fun isCommandDone(command: Commandable) = commandCache[command.uuid] != null

    @PreDestroy
    fun onDestroy() {
        /*declaredBindings.forEach {
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

        amqpAdmin.deleteExchange(EXCHANGE_COMMAND_GATEWAY)
        log.info("$EXCHANGE_COMMAND_GATEWAY named exchange is deleted from RabbitMq successfully")

        amqpAdmin.deleteExchange(EXCHANGE_EVENT_GATEWAY)
        log.info("$EXCHANGE_EVENT_GATEWAY named exchange is deleted from RabbitMq successfully")

        declaredQueues.clear()
        declaredBindings.clear()
        */
    }
}