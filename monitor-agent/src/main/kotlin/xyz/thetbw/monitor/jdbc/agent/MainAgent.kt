package xyz.thetbw.monitor.jdbc.agent

import xyz.thetbw.monitor.jdbc.lunch
import java.lang.instrument.Instrumentation
import java.lang.management.ManagementFactory

val logger = Logger.getLogger { }

const val MODE_PREMAIN = "pre_main"
const val MODE_AGENTMAIN = "agent_main"

fun premain(agentArgs: String?, inst: Instrumentation) {
    start(agentArgs, inst, MODE_PREMAIN)
}

fun agentmain(agentArgs: String?, inst: Instrumentation) {
    start(agentArgs, inst, MODE_AGENTMAIN)
}

var STANDALONE_MODE = false

fun main() {
    lunch(true)
}

fun start(agentArgs: String?, inst: Instrumentation, mode: String) {
    if (agentArgs?.contains("debug") == true) {
        Logger.level = Logger.Level.DEBUG
    }

    logger.println { "---------- monitor agent 开始加载 -----------" }
    logger.println { "当前启动模式为:$mode" }
    if (mode == MODE_PREMAIN) {
        lunchServer()
    }
    val pid = getCurrentPid()
    logger.info { "当前进程id为：$pid" }
    Client.init()
    Monitor.classProcess(inst)
    logger.println { "---------- monitor agent 加载成功 -----------" }
}

fun lunchServer() {
    logger.info { "开始启动agentServer" }
    lunch(false)
}

fun getCurrentPid(): String {
    val jvmName = ManagementFactory.getRuntimeMXBean().name
    return jvmName.split("@")[0]
}

fun exit() {
    Monitor.reset()
    Client.reset()
}
