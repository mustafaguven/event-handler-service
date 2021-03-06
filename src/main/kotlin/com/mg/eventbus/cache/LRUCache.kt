package com.mg.eventbus.cache

class LRUCache<T>(private val delegate: Cache<T> = PerpetualCache(), private val minimalSize: Int = DEFAULT_SIZE) : Cache<T> {
    private val keyMap = object : LinkedHashMap<Any, Any>(minimalSize, .75f, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<Any, Any>): Boolean {
            val tooManyCachedItems = size > minimalSize
            if (tooManyCachedItems) eldestKeyToRemove = eldest.key
            return tooManyCachedItems
        }
    }

    private var eldestKeyToRemove: Any? = null

    override val size: Int
        get() = delegate.size

    override fun set(key: Any, value: T) {
        delegate[key] = value
        cycleKeyMap(key)
    }

    override fun remove(key: Any) = delegate.remove(key)

    override fun get(key: Any): T? {
        keyMap[key]
        return delegate[key]
    }

    override fun clear() {
        keyMap.clear()
        delegate.clear()
    }

    private fun cycleKeyMap(key: Any) {
        keyMap[key] = PRESENT
        eldestKeyToRemove?.let { delegate.remove(it) }
        eldestKeyToRemove = null
    }

    companion object {
        private const val DEFAULT_SIZE = 1000
        private const val PRESENT = true
    }
}