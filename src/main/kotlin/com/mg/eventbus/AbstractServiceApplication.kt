package com.mg.eventbus

import com.mg.eventbus.cache.redis.RedisUtil
import com.mg.eventbus.gateway.EveCom
import com.mg.eventbus.mq.CommandMQConfig
import com.mg.eventbus.mq.EventMQConfig
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.cache.annotation.EnableCaching
import org.springframework.cloud.client.discovery.EnableDiscoveryClient
import org.springframework.cloud.netflix.hystrix.EnableHystrix
import org.springframework.cloud.netflix.hystrix.dashboard.EnableHystrixDashboard
import org.springframework.cloud.openfeign.EnableFeignClients
import org.springframework.context.annotation.Import
import org.springframework.context.event.EventListener


@EnableDiscoveryClient
@EnableHystrixDashboard
@EnableHystrix
@EnableFeignClients
@EnableCaching
@Import(EveCom::class, RedisUtil::class, CommandMQConfig::class, EventMQConfig::class)
abstract class MicroServiceApplication {

    @Autowired
    lateinit var eveCom: EveCom

    @EventListener(ApplicationReadyEvent::class)
    fun onApplicationReadyEvent() {
        eveCom.onApplicationReadyEvent(this.javaClass.`package`.name)
    }

}
