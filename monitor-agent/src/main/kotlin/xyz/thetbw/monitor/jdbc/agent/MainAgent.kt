package xyz.thetbw.monitor.jdbc.agent

import mu.KotlinLogging
import java.lang.instrument.Instrumentation
import java.lang.management.ManagementFactory

val logger = KotlinLogging.logger {  }

fun agentmain(agentArgs: String?,inst: Instrumentation){
    logger.info { "monitor agent 开始加载" }
    val pid = getCurrentPid()
    logger.info { "当前进程id为：$pid" }
    Client.init()
    Monitor.classProcess(inst)
    logger.info { "monitor agent 加载成功" }
}

fun getCurrentPid(): String{
    val jvmName = ManagementFactory.getRuntimeMXBean().getName()
    return jvmName.split("@")[0]
}

fun exit(){
    Monitor.reset()
    Client.reset()
}