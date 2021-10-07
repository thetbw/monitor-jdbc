package xyz.thetbw.monitor.jdbc.api

import io.ktor.application.*
import io.ktor.http.cio.websocket.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.websocket.*
import mu.KotlinLogging
import org.kodein.di.instance
import xyz.thetbw.monitor.jdbc.*
import xyz.thetbw.monitor.jdbc.service.AgentService
import xyz.thetbw.monitor.jdbc.service.LogService

fun Route.mainApi() {
    val logger = KotlinLogging.logger { }
    val agentService: AgentService by di.instance()
    val logService: LogService by di.instance()

    route("/process") {

        //获取当前所有java进程
        get {
            call.respond(ApiResult(agentService.listProcess()))
        }
        get("/attach") {
            val pid = call.request.queryParameters["pid"]
            val current = agentService.attachProcess(pid!!)
            call.respond(ApiResult(current))
        }
    }
    webSocket("/log/reader/{pid}") {
        logger.info { "一个新客户端连接" }
        send(SqlMessage("0", "连接成功", 0, 0, 0).toJson())
        logService.registerConsumers(this)
        for (frame in incoming) {
            when (frame) {
                is Frame.Text -> {
                    val text = frame.readText()
                    logger.info { "收到客户端消息：$text" }
                }
                else -> logger.warn { "不支持的消息类型" }
            }
        }
        /** 连接开始关闭 */
        logService.unregister(this)
    }

    webSocket("/log/producer") {
        logger.info { "一个agent连接" }
        send("agent连接成功")
        try {
            for (frame in incoming) {
                when (frame) {
                    is Frame.Text -> {
                        val text = frame.readText()
                        val message = text.fromJson<SqlMessage>()
                        logService.onMessage(message)
                    }
                    else -> logger.warn { "不支持的消息类型" }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

}