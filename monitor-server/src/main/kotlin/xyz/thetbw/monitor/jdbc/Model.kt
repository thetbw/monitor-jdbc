package xyz.thetbw.monitor.jdbc

import kotlinx.serialization.Serializable

/**
 * java进程描述
 */
@Serializable
data class JavaProcess(
    val pid: String, //进程pid
    val name: String, //进程名称
    val fullName: String, //进程全名
    val attached: Boolean //是否已经附加
)

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

@Serializable
@Suppress("unused")
data class ApiResult<T: Any>(val success: Boolean){

    var body: T? = null

    var msg: String? = null

    constructor(success: Boolean,msg: String) : this(success){
        this.msg = msg
    }

    constructor(body: T): this(true){
        this.body = body
    }
}