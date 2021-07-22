package xyz.thetbw.monitor.jdbc

import io.ktor.server.engine.*
import io.ktor.server.netty.*
import mu.KotlinLogging
import xyz.thetbw.monitor.jdbc.plugins.configureRouting
import xyz.thetbw.monitor.jdbc.plugins.configureSerialization
import xyz.thetbw.monitor.jdbc.plugins.configureWebSocket
import java.awt.Desktop
import java.net.URI

val logger = KotlinLogging.logger {  }

fun main() {
    embeddedServer(Netty, port = 10086, host = "127.0.0.1") {
        configureWebSocket()
        configureRouting()
        configureSerialization()
    }.start(wait = false)
    logger.info { "web server启动成功" }
    try {
        logger.info { "尝试打开浏览器" }
        Desktop.getDesktop().browse(URI.create("http://127.0.0.1:10086/"))
    }catch (e: Exception){
        logger.error(e) { "浏览器打开失败" }
    }
}
