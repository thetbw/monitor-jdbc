package xyz.thetbw.monitor.jdbc.agent

import mu.KotlinLogging
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import java.net.URI
import kotlin.concurrent.thread

/**
 * 负责和server之间的连接
 */
object Client {

    private val logger  =KotlinLogging.logger {  }
    private lateinit var client: WebSocketClient

    /** 初始化socket连接 */
    fun init(){
        logger.info { "开始连接服务器" }
        client = SocketClient(URI("http://localhost:10086/api/log/producer"))
        client.connect()
    }

    fun sendMessage(message: SqlMessage){
        if (client.isClosing || client.isClosed){
            return
        }
        if (client.isOpen){
            client.send(message.toJson())
        }else{
            logger.warn { "未连接到server" }
        }
    }

    fun reset(){
        client.closeBlocking()
    }

}

private class SocketClient(uri: URI): WebSocketClient(uri) {
    private var failureTimes: Int = 0

    override fun onOpen(handshakedata: ServerHandshake?) {
        logger.info { "websocket连接成功" }
    }

    override fun onMessage(message: String?) {
        logger.info { "收到来自服务器的消息:$message" }
        if (message == "exit"){
            exit()
        }
    }

    override fun onClose(code: Int, reason: String?, remote: Boolean) {
        logger.info { "连接已关闭" }
        exit()
    }

    override fun onError(ex: java.lang.Exception?) {
        logger.info (ex){ "websocket连接失败,尝试重新连接" }
        failureTimes++
        if (failureTimes < 5){
            logger.warn { "1s后尝试重新连接" }
            thread(start = true) {
                Thread.sleep(1000)
                this.reconnect()
            }
        }else{
            logger.error { "超过重试次数，关闭当前 agent" }
            exit()
        }
    }

}

