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
class CommandGateway(private val rabbitTemplate: RabbitTemplate,
                     val amqpAdmin: AmqpAdmin) {

    private val commandCache = LRUCache()
    private val declaredQueues: HashSet<Queue> by lazy { HashSet<Queue>() }
    private val declaredBindings: HashSet<Binding> by lazy { HashSet<Binding>() }

    companion object {
        private const val INTERVAL = 50L
        private const val MAX_TRYING = 100
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
            val queue = Queue(queueName)
            val binding = BindingBuilder.bind(queue).to(commandGatewayExchange()).with(queueName)
            amqpAdmin.declareQueue(queue)
            amqpAdmin.declareBinding(binding)
            declaredQueues.add(queue)
            declaredBindings.add(binding)
            log.info("$queueName named queue is created on RabbitMq successfully")
            log.info("$queueName named routing key is created on RabbitMq successfully")
        }
    }

    private fun <T : Commandable> convertAndSend(t: T) {
        rabbitTemplate.convertAndSend(COMMAND_GATEWAY_EXCHANGE, t.javaClass.simpleName, t)
        log.info("message sent to ${t.javaClass.simpleName} successfully")
    }

    fun onHandle(command: Commandable, func: () -> Any) {
        commandCache[command.uuid] = func()
    }

    fun send(command: Commandable) = CompletableFuture.supplyAsync<ResponseEntity<BaseResponse>> {
        convertAndSend(command)
        var trying = 0
        while (true) {
            trying++
            if (isCommandDone(command)) {
                break
            }
            if (trying > MAX_TRYING) {
                return@supplyAsync returnFailResponse(CommandTimeoutException(INTERVAL * MAX_TRYING))
            }
            Thread.sleep(INTERVAL)
        }
        return@supplyAsync returnSuccessResponse(command)
    }

    private fun returnSuccessResponse(command: Commandable): ResponseEntity<BaseResponse> {
        val response = BaseResponse()
        response.status = 1
        response.data = commandCache[command.uuid]
        return ResponseEntity.ok(response)
    }

    private fun returnFailResponse(ex: Exception): ResponseEntity<BaseResponse> {
        val response = BaseResponse()
        response.status = 0
        response.message = ex.message
        return ResponseEntity.ok(response)
    }

    private fun isCommandDone(command: Commandable) = commandCache[command.uuid] != null

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