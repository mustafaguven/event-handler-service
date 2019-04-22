package com.mg.eventbus.gateway

import com.mg.eventbus.Qualifiers
import com.mg.eventbus.cache.LRUCache
import com.mg.eventbus.exception.CommandTimeoutException
import com.mg.eventbus.inline.logger
import com.mg.eventbus.mq.CommandMQConfig
import com.mg.eventbus.mq.EventMQConfig
import com.mg.eventbus.response.BaseResponse
import lombok.extern.slf4j.Slf4j
import org.reflections.Reflections
import org.springframework.amqp.core.AmqpAdmin
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Component
import java.util.concurrent.CompletableFuture
import javax.annotation.PreDestroy


@Slf4j
@Component
class EveCom(val amqpAdmin: AmqpAdmin,
             @Qualifier(value = Qualifiers.COMMAND_MQ) val commandMQConfig: CommandMQConfig,
             @Qualifier(value = Qualifiers.EVENT_MQ) val eventMQConfig: EventMQConfig) {

    private val commandCache = LRUCache<Any>()

    companion object {
        private const val INTERVAL = 50L
        private const val MAX_TRYING = 100
        val log = logger(this)
    }

    fun onApplicationReadyEvent(packageName: String) {
        val reflections = Reflections(packageName)
        commandMQConfig.build(reflections)
        eventMQConfig.build(reflections)
    }

    private fun <T : Commandable> convertAndSendCommand(t: T) {
        commandMQConfig.convertAndSend(t)
    }

    private fun <T : Eventable> convertAndSendEvent(t: T) {
        eventMQConfig.convertAndSend(t)
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
//        commandMQConfig.deleteDeclaredBindings()
//        commandMQConfig.deleteDeclaredQueues()
//        commandMQConfig.deleteExchange()
//
//        eventMQConfig.deleteDeclaredBindings()
//        eventMQConfig.deleteDeclaredQueues()
//        eventMQConfig.deleteExchange()
    }
}