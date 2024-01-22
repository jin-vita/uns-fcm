package org.techtown.unsfcm

import io.lettuce.core.RedisClient
import io.lettuce.core.api.StatefulRedisConnection
import io.lettuce.core.api.sync.RedisCommands

class RedisManager {
    private val redisClient: RedisClient = RedisClient.create("redis://your_redis_server_ip:6379")
    private val connection: StatefulRedisConnection<String, String> = redisClient.connect()
    private val syncCommands: RedisCommands<String, String> = connection.sync()

    fun setTokenInRedis(token: String) {
        syncCommands.set("test01", token)
    }

    fun getValueFromRedis(key: String): String? {
        return syncCommands.get(key)
    }

    fun closeConnection() {
        connection.close()
        redisClient.shutdown()
    }
}