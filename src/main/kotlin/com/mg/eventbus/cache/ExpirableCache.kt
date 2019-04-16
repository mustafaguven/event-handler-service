package com.mg.eventbus.cache

import java.util.concurrent.TimeUnit

class ExpirableCache<T>(private val delegate: Cache<T> = PerpetualCache(),
                     private val flushInterval: Long = TimeUnit.SECONDS.toMillis(30)) : Cache<T> {
    private var lastFlushTime = System.nanoTime()

    override val size: Int
        get() = delegate.size

    override fun set(key: Any, value: T) {
        lastFlushTime = System.nanoTime()
        delegate[key] = value
    }

    override fun remove(key: Any): T? {
        recycle()
        return delegate.remove(key)
    }

    override fun get(key: Any): T? {
        recycle()
        return delegate[key]
    }


    override fun clear() = delegate.clear()

    private fun recycle() {
        val shouldRecycle = System.nanoTime() - lastFlushTime >= TimeUnit.MILLISECONDS.toNanos(flushInterval)
        if (!shouldRecycle) return
        delegate.clear()
    }
}