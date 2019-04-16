package com.mg.eventbus.inline

import org.slf4j.Logger
import org.slf4j.LoggerFactory

inline fun <reified T> logger(from: T): Logger {
    return LoggerFactory.getLogger(T::class.java)
}

inline fun <T : Any, R> whenNotNull(input: T?, callback: (T) -> R): R? {
    return input?.let(callback)
}