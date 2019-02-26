package com.mg.eventbus.producer

interface EventData<T> {
    val topic: String
    val routingKey: String
    val data: T
}