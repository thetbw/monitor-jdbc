package xyz.thetbw.monitor.jdbc.agent
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.features.websocket.*
import io.ktor.http.*
import io.ktor.http.cio.websocket.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import mu.KotlinLogging
import org.java_websocket.client.WebSocketClient
import org.java_websocket.drafts.Draft
import org.java_websocket.handshake.ServerHandshake
import java.net.URI

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
        if (client.isOpen){
            client.send(message.toJson())
        }else{
            logger.warn { "client还未连接" }
        }

    }
}

private class SocketClient(uri: URI): WebSocketClient(uri) {
    override fun onOpen(handshakedata: ServerHandshake?) {
        logger.info { "websocket连接成功" }
    }

    override fun onMessage(message: String?) {
        logger.info { "收到消息:$message" }
    }

    override fun onClose(code: Int, reason: String?, remote: Boolean) {
        logger.info { "连接已关闭" }
    }

    override fun onError(ex: java.lang.Exception?) {
        logger.info (ex){ "websocket连接失败" }
    }

}

