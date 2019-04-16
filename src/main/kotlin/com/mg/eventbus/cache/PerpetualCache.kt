package com.mg.eventbus.cache

class PerpetualCache<T> : Cache<T> {
    private val cache = HashMap<Any, T>()

    override val size: Int
        get() = cache.size

    override fun set(key: Any, value: T) {
        this.cache[key] = value
    }

    override fun remove(key: Any) = this.cache.remove(key)

    override fun get(key: Any) = this.cache[key]

    override fun clear() = this.cache.clear()
}