package com.mg.eventbus.mq

import com.mg.eventbus.gateway.Commandable
import com.mg.eventbus.gateway.Fireable
import com.mg.eventbus.inline.logger
import org.reflections.Reflections
import org.springframework.amqp.core.*
import org.springframework.amqp.rabbit.core.RabbitTemplate
import java.util.HashSet

abstract class MQConfig(protected val amqpAdmin: AmqpAdmin, val rabbitTemplate: RabbitTemplate) {

    private val declaredQueues: HashSet<Queue> by lazy { HashSet<Queue>() }
    private val declaredBindings: HashSet<Binding> by lazy { HashSet<Binding>() }

    companion object {
        val log = logger(this)
    }

    abstract val exchangeGatewayName: String

    abstract val queueClusterId: String

    private fun getQueueClusterIdWithSub() = queueClusterId.plus(".")

    private fun getQueueClusterRouteKey() = queueClusterId.plus(".*")

    abstract fun getExchange(): TopicExchange

    private fun createQueue(queueName: String? = null) {
        when (queueName) {
            null -> {
                makeQueue(queueClusterId, getQueueClusterRouteKey())
                createQueue(queueClusterId)
            }
            else -> {
                if (amqpAdmin.getQueueProperties(queueName) == null) {
                    makeQueue(queueName, queueName)
                }
            }
        }
    }

    private fun makeQueue(queueName: String, bindingName: String) {
        val queue = Queue(queueName)
        val binding = BindingBuilder.bind(queue).to(getExchange()).with(bindingName)
        amqpAdmin.declareQueue(queue)
        declaredQueues.add(queue)
        amqpAdmin.declareBinding(binding)
        declaredBindings.add(binding)
        log.info("$queueName named queue is created on RabbitMq successfully")
        log.info("$queueName named routing key is created on RabbitMq successfully")
    }

    fun getPreparedQueueName(clzName: String): String = getQueueClusterIdWithSub().plus(clzName)

    fun <T : Fireable> convertAndSend(t: T){
        val queueName = getPreparedQueueName(t.javaClass.simpleName)
        rabbitTemplate.convertAndSend(exchangeGatewayName, queueName, t)
        log.info("command sent to $queueName successfully")
    }

    abstract fun build(reflections: Reflections)

    protected fun <T> buildAmqp(classes: MutableSet<Class<out T>>) {
        createQueue()
        classes.forEach {
            createQueue(getPreparedQueueName(it.simpleName))
        }
    }

    fun deleteDeclaredBindings() {
        declaredBindings.takeWhile { it.routingKey.startsWith(queueClusterId) }.forEach {
            try {
                amqpAdmin.removeBinding(it)
                log.info("${it.routingKey} named binding is deleted from RabbitMq successfully")
            } catch (ex: Exception) {
                log.error(ex.message)
            }
        }
        declaredBindings.removeAll { it.routingKey.startsWith(queueClusterId) }
    }

    fun deleteDeclaredQueues() {
        declaredQueues.takeWhile { it.name.startsWith(queueClusterId) }.forEach {
            try {
                amqpAdmin.deleteQueue(it.name)
                log.info("${it.name} named queue is deleted from RabbitMq successfully")
            } catch (ex: Exception) {
                log.error(ex.message)
            }
        }
        declaredQueues.removeAll { it.name.startsWith(queueClusterId) }
    }

    fun deleteExchange() {
        amqpAdmin.deleteExchange(exchangeGatewayName)
        log.info("$exchangeGatewayName named exchange is deleted from RabbitMq successfully")
    }

}