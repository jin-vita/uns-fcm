package org.techtown.unsfcm

import io.lettuce.core.RedisClient
import io.lettuce.core.api.StatefulRedisConnection
import io.lettuce.core.api.sync.RedisCommands

class RedisManager {
    private val redisClient: RedisClient = RedisClient.create("redis://${AppData.REDIS_HOST}:${AppData.REDIS_PORT}")
    private val connection: StatefulRedisConnection<String, String> = redisClient.connect()
    private val syncCommands: RedisCommands<String, String> = connection.sync()

    fun set(channel: String, token: String) {
        syncCommands.set(channel, token)
    }

    fun get(key: String): String? {
        return syncCommands.get(key)
    }

    fun close() {
        connection.close()
        redisClient.shutdown()
    }
}