package xyz.thetbw.monitor.jdbc.agent

import kotlinx.serialization.Serializable

/**
 * sql执行消息
 */
@Serializable
data class SqlMessage(
    val pid: String,
    val sql: String,
    val startTime: Long,
    val endTime: Long,
    val costTime: Long
)