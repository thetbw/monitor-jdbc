package xyz.thetbw.monitor.jdbc.service

import com.sun.tools.attach.VirtualMachine
import mu.KotlinLogging
import xyz.thetbw.monitor.jdbc.JavaProcess
import java.io.File
import java.io.FileOutputStream
import kotlin.collections.ArrayList
import kotlin.collections.HashSet


class AgentService {

    private val logger = KotlinLogging.logger {  }

    private val attachVms = HashSet<String>() //已经连接的vm

    /** agent的jar包路径 */
    private var agentPath = lazy{unzipAgentJar()}

    /**
     * 获取java进程列表
     */
    fun listProcess(): List<JavaProcess>{
        val vms = VirtualMachine.list()
        val javaProcesses = ArrayList<JavaProcess>()
        vms.forEach{
            val pid =  it.id()
            val name = it.displayName()
            val attached = attachVms.contains(pid)
            javaProcesses.add(JavaProcess(pid,name,name,attached))
        }
        return javaProcesses
    }

    /**
     * 附加到一个进程上
     */
    fun attachProcess(pid: String){
        val vm  = VirtualMachine.attach(pid)
        val agentPath = agentPath.value
        logger.info { "开始加载agent,当前agent地址为:$agentPath" }
        try {
            vm.loadAgent(agentPath)
        }catch (e:Exception){
            if(e.message == "0"){
                logger.info { "agent加载成功" }
                attachVms.add(pid)
            }else{
                logger.error(e) { "agent加载失败" }
            }
        }

    }


    private fun unzipAgentJar():String{
        logger.info { "开始解压agent" }
        val tempPath = getTempPath();
        val outFile = File(File(tempPath),"agent.jar")
        val outPath = outFile.absolutePath
        logger.info { "当前agent解压目录：$outPath" }
        if (outFile.exists()){
            logger.warn { "agent已经存在，正在删除" }
            outFile.delete();
        }

        val jarStream = this.javaClass.classLoader.getResourceAsStream("agent.jar")
        jarStream.use {
            val outStream = FileOutputStream(outFile)
            outStream.use {
                var buffer = ByteArray(2048)
                var length: Int
                while ( jarStream.read(buffer).also { length = it } != -1){
                    outStream.write(buffer,0,length)
                }
            }
        }
        return outPath
    }

    //获取临时目录
    private fun getTempPath(): String{
        var tempPath = System.getProperty("java.io.tmpdir")
        if (tempPath.isBlank()){
            tempPath = ""
        }
        return tempPath;
    }


}