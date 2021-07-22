package xyz.thetbw.monitor.jdbc

import io.ktor.server.engine.*
import io.ktor.server.netty.*
import mu.KotlinLogging
import xyz.thetbw.monitor.jdbc.plugins.configureRouting
import xyz.thetbw.monitor.jdbc.plugins.configureSerialization
import xyz.thetbw.monitor.jdbc.plugins.configureWebSocket
import java.awt.Desktop
import java.io.File
import java.net.URI
import java.net.URL
import java.net.URLClassLoader

val logger = KotlinLogging.logger {  }

fun main() {
    loadToolsJar()
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

//手动加载 toots.jar
fun loadToolsJar(){
    try {
        logger.info { "尝试手动加载 tools.jar" }
        val javaHome = System.getProperty("java.home")
        logger.info { "当前java_home: $javaHome" }
        var libDir = File(javaHome)
        if (javaHome.endsWith("jre")){
            libDir = libDir.parentFile
        }
        libDir = File(libDir,"lib")
        if (!libDir.exists()){
            error("查到tools.jar 失败，当前目录：${libDir.path}")
        }
        val jar = File(libDir,"tools.jar")
        if (!libDir.exists()){
            error("查到tools.jar 失败，当前目录：${libDir.path}")
        }
        logger.info { "开始加载jar: ${jar.path}" }
        val loader = ClassLoader.getSystemClassLoader() as URLClassLoader
        val method = URLClassLoader::class.java.getDeclaredMethod("addURL", URL::class.java)
        method.isAccessible = true
        method.invoke(loader,jar.toURI().toURL())
    }catch (e: Exception){
        logger.error(e) { "加载 tools.jar失败，请检查当前 jvm版本，" +
                "或者通过启动时给定参数 '-Xbootclasspath/a:<jarpath>' 来手动加载" }
    }
}
