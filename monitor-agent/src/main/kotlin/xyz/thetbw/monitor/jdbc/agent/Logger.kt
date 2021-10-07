package xyz.thetbw.monitor.jdbc.agent

/**
 * 简单的日志记录 单独写一个而不用 slf4是因为不想和 attach 目标的配置冲突
 */
class Logger(private val className: String) {


    private val simpleName: String

    init {
        val nameArray = className.split(".").toMutableList()
        nameArray.indices.forEach {
            if (it != nameArray.size - 1) {
                nameArray[it] = nameArray[it].substring(0, 1)
            }
        }
        simpleName = nameArray.joinToString(".")
    }

    companion object {
        var level = Level.INFO

        /** 获取一个 logger 记录器 */
        inline fun getLogger(noinline func: () -> Unit): Logger {
            val name = func.javaClass.name
            return Logger(name)
        }
    }


    inline fun println(provider: () -> String) {
        println(provider())
    }

    inline fun debug(provider: () -> String) {
        if (level.index > Level.DEBUG.index) return
        log("debug", provider())
    }

    inline fun info(provider: () -> String) {
        if (level.index > Level.INFO.index) return
        log("info", provider())
    }

    inline fun warn(provider: () -> String) {
        if (level.index > Level.WARN.index) return
        log("warn", provider())
    }

    inline fun warn(exception: Exception, provider: () -> String) {
        if (level.index > Level.WARN.index) return
        exception.printStackTrace()
        log("warn", provider())
    }

    inline fun error(provider: () -> String) {
        if (level.index > Level.ERROR.index) return
        log("error", provider())
    }

    inline fun error(exception: Exception, provider: () -> String) {
        if (level.index > Level.ERROR.index) return
        exception.printStackTrace()
        log("error", provider())
    }


    fun log(type: String, msg: String) {
        println("[$type] $simpleName: $msg")
    }


    enum class Level(val index: Int) {
        DEBUG(0), INFO(1), WARN(2), ERROR(3)
    }
}
