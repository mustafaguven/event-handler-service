package com.mg.eventbus.cache.redis

import org.springframework.context.annotation.Bean
import org.springframework.data.redis.connection.RedisStandaloneConfiguration
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer
import org.springframework.data.redis.serializer.StringRedisSerializer
import org.springframework.stereotype.Component


@Component
class RedisUtil {

    companion object {
        private const val LOCALHOST = "localhost"
        private const val ENV_REDIS_HOSTNAME = "ENV_REDIS_HOSTNAME"
        private const val BEARER = "Bearer "
        private const val EMPTY_STRING = ""
    }

    @Bean
    private fun jedisConnectionFactory(): JedisConnectionFactory {
        val redisConf = RedisStandaloneConfiguration()
        redisConf.hostName = System.getenv(ENV_REDIS_HOSTNAME) ?: LOCALHOST
        return JedisConnectionFactory(redisConf)
    }

    @Bean
    fun redisTemplate(): RedisTemplate<String, Any> {
        val redisTemplate = RedisTemplate<String, Any>()
        redisTemplate.connectionFactory = jedisConnectionFactory()
        redisTemplate.defaultSerializer = GenericJackson2JsonRedisSerializer()
        redisTemplate.keySerializer = StringRedisSerializer()
        redisTemplate.hashKeySerializer = GenericJackson2JsonRedisSerializer()
        redisTemplate.valueSerializer = GenericJackson2JsonRedisSerializer()
        redisTemplate.setEnableTransactionSupport(true)

        redisTemplate.afterPropertiesSet()
        return redisTemplate
    }

}
