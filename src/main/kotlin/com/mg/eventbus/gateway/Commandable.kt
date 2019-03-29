package com.mg.eventbus.gateway

import java.util.*

abstract class Commandable {
    val uuid: UUID = UUID.randomUUID()

    companion object {
        const val QUEUE_CLUSTER_ID = CommandGateway.QUEUE_CLUSTER_ID.plus(".")
    }

}