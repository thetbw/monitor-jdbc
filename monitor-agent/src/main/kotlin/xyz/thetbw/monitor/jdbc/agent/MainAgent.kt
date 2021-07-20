package xyz.thetbw.monitor.jdbc.agent

import mu.KotlinLogging
import java.lang.instrument.Instrumentation
import java.lang.management.ManagementFactory

val logger = KotlinLogging.logger {  }

fun agentmain(agentArgs: String?,inst: Instrumentation){
    logger.info { "agent方法开始执行" }
    val pid = getCurrentPid();
    logger.info { "当前进程id为：$pid" }
    Client.init()
    testA()
}

fun testA(){
    Thread.sleep(1000)
    logger.info { "开始测试websocket发送消息" }
    repeat(1000){
        val message = SqlMessage(getCurrentPid(),"text",System.currentTimeMillis(),System.currentTimeMillis(),0)
        Client.sendMessage(message)
        Thread.sleep(1000)
    }
}

fun getCurrentPid(): String{
    val jvmName = ManagementFactory.getRuntimeMXBean().getName()
    return jvmName.split("@")[0]
}