package xyz.thetbw.monitor.jdbc.agent

import mu.KotlinLogging
import xyz.thetbw.monitor.jdbc.agent.transformers.JdbcTransformer
import xyz.thetbw.monitor.jdbc.agent.transformers.MysqlTransformer
import java.lang.instrument.Instrumentation
import java.sql.DriverManager
import java.util.concurrent.Executors


object Monitor {
    private val logger = KotlinLogging.logger {  }

    private val pool = Executors.newSingleThreadExecutor()
    private lateinit var inst: Instrumentation

    // 当前支持的驱动程序包名
    private val transformers = ArrayList<JdbcTransformer>().apply {
        add(MysqlTransformer())
    }

    //已经转换的类,在撤销时还原
    private var convertedClasses = ArrayList<Class<*>>()

    fun classProcess(inst: Instrumentation){
        this.inst = inst
        logger.info { "开始加载 transformers" }
        transformers.forEach{
            logger.info { "transformer -> ${it.javaClass.name}" }
            inst.addTransformer(it,true)
        }

        logger.info { "开始扫描 jdbc 驱动" }

        val drivers = DriverManager.getDrivers().toList()
        if (drivers.isEmpty()){
            logger.error { "没有找到 jdbc 驱动，退出执行" }
            exit()
            return
        }
        val needTransClasses = ArrayList<Class<*>>()
        drivers.forEach {driver->
            var support = false
            transformers.forEach{ transformer ->
                if (transformer.isSupport(driver.javaClass.name,driver.majorVersion,driver.minorVersion)){
                    transformer.needTransClassesName().forEach {
                        try {
                            val needTransClass = Class.forName(it)
                            needTransClasses.add(needTransClass)
                        }catch (e: ClassNotFoundException){
                            logger.error(e) { "找不到需要转换的类" }
                        }
                    }
                    support = true
                }
            }
            if (!support){
                logger.warn { "当前驱动暂时不支持: ${driver.javaClass.name}" }
            }
        }
        if (needTransClasses.isEmpty()){
            logger.error { "没有找到需要处理的类，退出执行" }
            exit()
            return
        }
        logger.info { "开始转换以下class" }
        convertedClasses = needTransClasses
        needTransClasses.forEach {
            logger.info { "class -> ${it.name}" }
            inst.retransformClasses(it)
        }
    }

    fun reset(){
        logger.warn { "开始重置转换后的类" }
        transformers.forEach{
            inst.removeTransformer(it)
        }
        convertedClasses.forEach {
            inst.retransformClasses(it)
        }
        convertedClasses.clear()
        pool.shutdownNow()
    }

    /**
     * 开始记录 sql 执行情况
     */
    @Suppress("unused")
    fun record(sqlByte: ByteArray,startTime: Long){
        try {
            val endTime = System.currentTimeMillis()
            val sql  = getSql(sqlByte)
            val pid = getCurrentPid()
            val costTime = endTime - startTime
            val sqlMessage = SqlMessage(pid, sql, startTime, endTime, costTime)
            if (!pool.isShutdown){
                saveRecord(sqlMessage)
            }
        }catch (e: Exception){
            logger.error(e) { "统计sql信息出现了异常" }
        }
    }


    /** 将packet byte 转换为 sql字符串 */
    private fun getSql(sqlByte: ByteArray): String{
        var beginIndex = -1
        var endIndex = -1
        for ((index,byte) in sqlByte.withIndex()) {
            val byteInt = byte.toInt()
            if (beginIndex == -1 && byte in 48..122){
                beginIndex = index
            }
            if (beginIndex != -1 && endIndex == -1 && byteInt == 0){
                endIndex = index
            }
            if (byteInt == 0x0A || byteInt == 0x09 || byteInt == 0x0D){
                sqlByte[index] = 0x20
            }
        }
        if (endIndex == -1) endIndex = sqlByte.size
        if (beginIndex == -1 ) beginIndex = 0
        return String(sqlByte,beginIndex,endIndex - beginIndex)
    }


    private fun saveRecord(message: SqlMessage) = pool.execute{
        Client.sendMessage(message)
    }


}