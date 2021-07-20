package xyz.thetbw.monitor.jdbc

import io.ktor.server.engine.*
import io.ktor.server.netty.*
import mu.KotlinLogging
import xyz.thetbw.monitor.jdbc.plugins.*

val logger = KotlinLogging.logger {  }

fun main() {
    embeddedServer(Netty, port = 10086, host = "0.0.0.0") {
        configureWebSocket()
        configureRouting()
        configureTemplating()
        configureSerialization()
    }.start(wait = false)
    logger.info { "web server启动成功" }
}
