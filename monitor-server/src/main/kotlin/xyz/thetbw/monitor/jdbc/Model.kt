package xyz.thetbw.monitor.jdbc

/**
 * java进程描述
 */
data class JavaProcess(
    val pid: String, //进程pid
    val name: String, //进程名称
    val fullName: String, //进程全名
    val attached: Boolean //是否已经附加
)

/**
 * sql执行消息
 */
data class SqlMessage(
    val pid: String,
    val sql: String,
    val startTime: Long,
    val endTime: Long,
    val costTime: Long
)