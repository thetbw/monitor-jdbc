package xyz.thetbw.monitor.jdbc.agent

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

fun start(agentArgs: String?, inst: Instrumentation, mode: String) {
    Logger.level = Logger.Level.DEBUG
    logger.println { "---------- monitor agent 开始加载 -----------" }
    logger.println { "当前启动模式为:$mode" }

    val pid = getCurrentPid()
    logger.info { "当前进程id为：$pid" }
    Client.init()
    Monitor.classProcess(inst)
    logger.println { "---------- monitor agent 加载成功 -----------" }
}

fun getCurrentPid(): String {
    val jvmName = ManagementFactory.getRuntimeMXBean().name
    return jvmName.split("@")[0]
}

fun exit() {
    Monitor.reset()
    Client.reset()
}
