package xyz.thetbw.monitor.jdbc.agent.transformers

import javassist.ClassPool
import javassist.CtClass
import javassist.NotFoundException
import mu.KotlinLogging

class MysqlTransformer: JdbcTransformer() {

    private val logger = KotlinLogging.logger {  }

    override fun isSupport(driveName: String, majorVersion: Int, minorVersion: Int): Boolean {
        return ( driveName == "com.mysql.cj.jdbc.Driver" || driveName == "com.mysql.jdbc.Driver")
                && (majorVersion == 5 || majorVersion == 8)
    }

    override fun needTransClassesName(): Set<String> = HashSet<String>().apply {
        add("com.mysql.cj.NativeSession") //mysql 8.* 驱动需要装换的class
        add("com.mysql.jdbc.ConnectionImpl") //mysql 5.* 需要转换的 class
    }

    override fun transform(className: String, originClassBuffer: ByteArray): ByteArray {
        return when(className){
            "com.mysql.cj.NativeSession" -> mysql8TransHandler(originClassBuffer)
            "com.mysql.jdbc.ConnectionImpl" -> mysql5TransHandler(originClassBuffer)
            else -> originClassBuffer
        }
    }

    private fun mysql8TransHandler(originClassBuffer: ByteArray): ByteArray{
        val pool = ClassPool.getDefault()
        var clazz: CtClass ? = null
        try {
            logger.info { "开始处理类" }
            clazz = pool.get("com.mysql.cj.NativeSession")
            val method = clazz.getDeclaredMethod("execSQL")
            method.addLocalVariable("startTime",CtClass.longType)
            method.insertBefore("{startTime = System.currentTimeMillis();}")
            method.insertAfter("{xyz.thetbw.monitor.jdbc.agent.Monitor.INSTANCE.record($4.getByteBuffer(),startTime);}")
            return clazz.toBytecode()
        }catch (e: NotFoundException){
            logger.error(e) { "class 加载失败" }
        }catch (e: Exception){
            logger.error(e) { "class 处理异常" }
        }finally {
            clazz?.detach()
        }
        return originClassBuffer
    }

    private fun  mysql5TransHandler(originClassBuffer: ByteArray): ByteArray{
        val pool = ClassPool.getDefault()
        var clazz: CtClass ? = null
        try {
            logger.info { "开始处理类" }
            clazz = pool.get("com.mysql.jdbc.ConnectionImpl")
            val methods = clazz.getDeclaredMethods("execSQL").toList()
            methods.sortedBy { it.parameterTypes.size } //取参数最多的那个方法
            val method = methods[methods.size-1]
            method.addLocalVariable("startTime",CtClass.longType)
            method.insertBefore("{startTime = System.currentTimeMillis();}")
            method.insertAfter("{xyz.thetbw.monitor.jdbc.agent.Monitor.INSTANCE.record($4.getByteBuffer(),startTime);}")
            return clazz.toBytecode()
        }catch (e: NotFoundException){
            logger.error(e) { "class 加载失败" }
        }catch (e: Exception){
            logger.error(e) { "class 处理异常" }
        }finally {
            clazz?.detach()
        }
        return originClassBuffer
    }
}