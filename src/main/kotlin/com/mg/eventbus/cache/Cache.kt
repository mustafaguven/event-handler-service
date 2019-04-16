package com.mg.eventbus.cache

interface Cache<T> {
    val size: Int

    operator fun set(key: Any, value: T)

    operator fun get(key: Any): T?

    fun remove(key: Any): T?

    fun clear()
}