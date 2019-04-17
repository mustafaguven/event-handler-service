package com.mg.eventbus.gateway

import java.util.*

abstract class Eventable(val entity: Any?) {
    val uuid: UUID = UUID.randomUUID()

    companion object {
        const val ENTITY = "entity"
        const val QUEUE_EVENT_CLUSTER_ID: String = EveCom.QUEUE_EVENT_CLUSTER_ID.plus(".")
    }
}