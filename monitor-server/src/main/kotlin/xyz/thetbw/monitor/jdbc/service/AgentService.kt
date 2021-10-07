package xyz.thetbw.monitor.jdbc.service

import com.sun.tools.attach.VirtualMachine
import mu.KotlinLogging
import xyz.thetbw.monitor.jdbc.JavaProcess
import java.io.File
import java.io.FileOutputStream


class AgentService {

    private val logger = KotlinLogging.logger { }

    private val attachVms = HashSet<String>() //已经连接的vm

    /** agent的jar包路径 */
    private var agentPath = lazy { unzipAgentJar() }

    /**
     * 获取java进程列表
     */
    fun listProcess(): List<JavaProcess> {
        val vms = VirtualMachine.list()
        val javaProcesses = ArrayList<JavaProcess>()
        vms.forEach {
            val pid = it.id()
            val name = it.displayName()
            val attached = attachVms.contains(pid)
            javaProcesses.add(JavaProcess(pid, name, name, attached))
        }
        return javaProcesses
    }

    /**
     * 附加到一个进程上
     */
    fun attachProcess(pid: String): JavaProcess {
        val javaProcess = listProcess()
        var currentProcess: JavaProcess? = null
        javaProcess.forEach {
            if (pid == it.pid) {
                currentProcess = it
            }
        }
        if (currentProcess == null) {
            error("没有找到当前进程或者当前不是一个java进程")
        }

        if (attachVms.contains(pid)) {
            logger.warn { "当前进程($pid) 已连接，跳过连接" }
            return currentProcess!!
        }

        val vm = VirtualMachine.attach(pid)

        //判断虚拟机版本
        val currentVMVersion = System.getProperty("java.version")
        val targetVMVersion = vm.systemProperties.getProperty("java.version")
        if (currentVMVersion != targetVMVersion) {
            error("当前虚拟机版本($currentVMVersion)和目标虚拟机版本不一致($targetVMVersion)")
        }
        val agentPath = agentPath.value
        logger.info { "开始加载agent,当前agent地址为:$agentPath" }
        try {
            logger.info { "当前 pid 为: $pid" }
            vm.loadAgent(agentPath)
            attachVms.add(pid)
        } catch (e: Exception) {
            if (e.message == "0") {
                logger.info { "agent加载成功" }
            } else {
                logger.error(e) { "agent加载失败" }
                error("agent加载失败")
            }
        } finally {
            vm.detach()
        }
        return currentProcess!!
    }


    private fun unzipAgentJar(): String {
        logger.info { "开始解压agent" }
        val tempPath = getTempPath()
        val outFile = File(File(tempPath), "agent.jar")
        val outPath = outFile.absolutePath
        logger.info { "当前agent解压目录：$outPath" }
        if (outFile.exists()) {
            logger.warn { "agent已经存在，正在删除" }
            outFile.delete()
        }

        val jarStream =
            this.javaClass.classLoader.getResourceAsStream("agent.jar") ?: throw RuntimeException("没有找到agent.jar")
        jarStream.use {
            val outStream = FileOutputStream(outFile)
            outStream.use {
                val buffer = ByteArray(2048)
                var length: Int
                while (jarStream.read(buffer).also { length = it } != -1) {
                    outStream.write(buffer, 0, length)
                }
            }
        }
        return outPath
    }

    //获取临时目录
    private fun getTempPath(): String {
        var tempPath = System.getProperty("java.io.tmpdir")
        if (tempPath.isBlank()) {
            tempPath = ""
        }
        return tempPath
    }


}
