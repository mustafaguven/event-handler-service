package com.mg.eventbus.gateway

import java.util.*

abstract class Commandable {
    var uuid: UUID = UUID.randomUUID()
}