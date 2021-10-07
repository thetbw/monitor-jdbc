package xyz.thetbw.monitor.jdbc.agent

import xyz.thetbw.monitor.jdbc.agent.transformers.JdbcTransformer
import xyz.thetbw.monitor.jdbc.agent.transformers.MysqlTransformer
import java.lang.instrument.Instrumentation
import java.sql.Driver
import java.sql.DriverManager
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.Executors


object Monitor {
    private val logger = Logger.getLogger { }

    private val pool = Executors.newSingleThreadExecutor()
    private lateinit var inst: Instrumentation

    // 当前支持的驱动程序包名
    private val transformers = ArrayList<JdbcTransformer>().apply {
        add(MysqlTransformer())
    }

    //已经转换的类,在撤销时还原
    private var convertedClasses = ArrayList<Class<*>>()

    fun classProcess(inst: Instrumentation) {
        this.inst = inst
        logger.info { "开始加载 transformers" }
        transformers.forEach {
            logger.info { "transformer -> ${it.javaClass.name}" }
            inst.addTransformer(it, true)
        }

        logger.info { "开始扫描 jdbc 驱动" }

        val drivers = DriverManager.getDrivers().toList().toMutableList()
        if (drivers.isEmpty()) {
            logger.warn { "没有找到 jdbc 驱动，尝试通过反射获取" }
            //通过反射获取所有注册的 drivers
            val driverRegisterField = DriverManager::class.java.getDeclaredField("registeredDrivers")
            driverRegisterField.isAccessible = true
            val driverInfos = driverRegisterField.get(DriverManager::class.java) as CopyOnWriteArrayList<*>
            if (!driverInfos.isEmpty()) {
                driverInfos.forEach {
                    val driverField = it::class.java.getDeclaredField("driver")
                    driverField.isAccessible = true
                    drivers.add((driverField.get(it) as Driver))
                }
            }
        }

        if (drivers.isEmpty()) {
            logger.error { "没有找到 jdbc 驱动，退出执行" }
            exit()
            return
        }
        val needTransClasses = ArrayList<Class<*>>()
        drivers.forEach { driver ->
            var support = false
            transformers.forEach { transformer ->
                if (transformer.isSupport(driver.javaClass.name, driver.majorVersion, driver.minorVersion)) {
                    transformer.needTransClassesName().forEach {
                        try {
                            val needTransClass = Class.forName(it)
                            needTransClasses.add(needTransClass)
                        } catch (e: ClassNotFoundException) {
                            logger.warn { "没有找到对应 class: $it" }
                        }
                    }
                    support = true
                }
            }
            if (!support) {
                logger.warn { "当前驱动暂时不支持: ${driver.javaClass.name}" }
            }
        }
        if (needTransClasses.isEmpty()) {
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

    fun reset() {
        logger.warn { "开始重置转换后的类" }
        transformers.forEach {
            inst.removeTransformer(it)
        }
        convertedClasses.forEach {
            inst.retransformClasses(it)
        }
        convertedClasses.clear()
        pool.shutdownNow()
    }


    fun saveRecord(sql: String, startTime: Long, endTime: Long = System.currentTimeMillis()) {
        val message: SqlMessage = SqlMessage(
            getCurrentPid(),
            sql,
            startTime,
            endTime,
            endTime - startTime,
        )
        saveRecord(message);
    }

    private fun saveRecord(message: SqlMessage) = pool.execute {
        logger.debug { "开始发送 sql 记录：${message.sql}" }
        Client.sendMessage(message)
    }


}
