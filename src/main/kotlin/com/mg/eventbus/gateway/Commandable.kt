package com.mg.eventbus.gateway

import java.util.*

abstract class Commandable(val entity: Any?) {
    val uuid: UUID = UUID.randomUUID()

    companion object {
        const val ENTITY = "entity"
        const val QUEUE_COMMAND_CLUSTER_ID: String = EveCom.QUEUE_COMMAND_CLUSTER_ID.plus(".")
    }
}