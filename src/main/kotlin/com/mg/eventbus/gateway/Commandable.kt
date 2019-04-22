package com.mg.eventbus.gateway

import com.mg.eventbus.mq.CommandMQConfig
import java.util.*

abstract class Commandable(val entity: Any?) : Fireable {
    val uuid: UUID = UUID.randomUUID()

    companion object {
        const val ENTITY = "entity"
        const val QUEUE_COMMAND_CLUSTER_ID: String = CommandMQConfig.QUEUE_CLUSTER_ID.plus(".")
    }
}