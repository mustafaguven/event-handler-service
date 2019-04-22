package com.mg.eventbus.gateway

import com.mg.eventbus.mq.EventMQConfig
import java.util.*

abstract class Eventable(val entity: Any?) : Fireable {
    val uuid: UUID = UUID.randomUUID()

    companion object {
        const val ENTITY = "entity"
        const val QUEUE_EVENT_CLUSTER_ID: String = EventMQConfig.QUEUE_CLUSTER_ID.plus(".")
    }
}