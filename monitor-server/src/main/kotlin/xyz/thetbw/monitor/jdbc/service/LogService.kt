package xyz.thetbw.monitor.jdbc.service

import io.ktor.http.cio.websocket.*
import io.ktor.websocket.*
import xyz.thetbw.monitor.jdbc.SqlMessage
import xyz.thetbw.monitor.jdbc.toJson

class LogService {
    private val consumers = ArrayList<DefaultWebSocketServerSession>()



    fun registerConsumers(consumer: DefaultWebSocketServerSession){
        synchronized(consumers){
            consumers.add(consumer)
        }
    }

    fun unregister(consumer: DefaultWebSocketServerSession){
        consumers.remove(consumer)
    }

    suspend fun onMessage(message: SqlMessage){
        consumers.forEach{
            val pid = it.call.parameters["pid"];
            if (pid == message.pid){
                it.outgoing.send(Frame.Text(message.toJson()))
            }

        }
    }
}