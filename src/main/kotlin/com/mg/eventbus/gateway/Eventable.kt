package com.mg.eventbus.gateway

import java.util.*

abstract class Eventable {
    val uuid: UUID = UUID.randomUUID()

    companion object {
        const val ENTITY = "entity"
        const val QUEUE_CLUSTER_ID: String = CommandGateway.QUEUE_COMMAND_CLUSTER_ID.plus(".")
    }
}