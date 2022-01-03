package xyz.thetbw.monitor.jdbc.agent.transformers

import javassist.ClassPool
import javassist.CtClass
import javassist.NotFoundException
import xyz.thetbw.monitor.jdbc.agent.Logger
import xyz.thetbw.monitor.jdbc.agent.Monitor
import xyz.thetbw.monitor.jdbc.agent.logger
import java.lang.reflect.Field
import java.lang.reflect.Method


class MysqlTransformer : JdbcTransformer() {

    private val logger = Logger.getLogger { }

    override fun isSupport(driveName: String, majorVersion: Int, minorVersion: Int): Boolean {
        return (driveName == "com.mysql.cj.jdbc.Driver" || driveName == "com.mysql.jdbc.Driver")
                && (majorVersion == 5 || majorVersion == 8)
    }

    override fun needTransClassesName(): Set<String> = HashSet<String>().apply {
        add("com.mysql.cj.NativeSession") //mysql 8.* 驱动需要装换的class
        add("com.mysql.jdbc.ConnectionImpl") //mysql 5.* 需要转换的 class
    }

    override fun transform(className: String, originClassBuffer: ByteArray): ByteArray {
        val byteCode = when (className) {
            "com.mysql.cj.NativeSession" -> mysql8TransHandler(originClassBuffer)
            "com.mysql.jdbc.ConnectionImpl" -> mysql5TransHandler(originClassBuffer)
            else -> originClassBuffer
        }
        return byteCode
    }

    private fun mysql8TransHandler(originClassBuffer: ByteArray): ByteArray {
        val pool = ClassPool.getDefault()
        var clazz: CtClass? = null
        try {
            logger.info { "开始处理类" }
            clazz = pool.get("com.mysql.cj.NativeSession")
            val method = clazz.getDeclaredMethod("execSQL")
            method.addLocalVariable("startTime", CtClass.longType)
            method.insertBefore("{startTime = System.currentTimeMillis();}")
            method.insertAfter("{xyz.thetbw.monitor.jdbc.agent.transformers.MysqlTransformer.mysql8SqlHandle($4,startTime,this);}")
            return clazz.toBytecode()
        } catch (e: NotFoundException) {
            logger.error(e) { "class 加载失败" }
        } catch (e: Exception) {
            logger.error(e) { "class 处理异常" }
        } finally {
            clazz?.detach()
        }
        return originClassBuffer
    }

    private fun mysql5TransHandler(originClassBuffer: ByteArray): ByteArray {
        val pool = ClassPool.getDefault()
        var clazz: CtClass? = null
        try {
            logger.info { "开始处理类" }
            clazz = pool.get("com.mysql.jdbc.ConnectionImpl")
            val methods = clazz.getDeclaredMethods("execSQL").toList()
            methods.sortedBy { it.parameterTypes.size } //取参数最多的那个方法
            val method = methods[methods.size - 1]
            method.addLocalVariable("startTime", CtClass.longType)
            method.insertBefore("{startTime = System.currentTimeMillis();}")
            method.insertAfter("{xyz.thetbw.monitor.jdbc.agent.transformers.MysqlTransformer.mysql5SqlHandle($4,startTime);}")
            return clazz.toBytecode()
        } catch (e: NotFoundException) {
            logger.error(e) { "class 加载失败" }
        } catch (e: Exception) {
            logger.error(e) { "class 处理异常" }
        } finally {
            clazz?.detach()
        }
        return originClassBuffer
    }


    companion object {

        @JvmStatic
        fun mysql8SqlHandle(queryPackage: Any?, startTime: Long, nativeSession: Any) {
            if (queryPackage == null) return
            val endTime = System.currentTimeMillis() //执行结束时间
            val byteBuffer = getFieldValue("byteBuffer", queryPackage) as ByteArray
            val propertySet = getFieldValue("propertySet", nativeSession)!!
            val maxQuerySizeToLog =
                execMethod("getIntegerProperty", propertySet, "maxQuerySizeToLog", String::class.java)!!

            //开始解码sql
            val queryPosition = try {
                execMethod("getTag", queryPackage, "QUERY") as Int
            } catch (e: Exception) {
                1
            }
            val oldPacketPosition = execMethod("getPosition", queryPackage, null) as Int
            val maxQuerySizeToLogValue = execMethod("getValue", maxQuerySizeToLog, null) as Int
            val truncated: Boolean = oldPacketPosition - queryPosition > maxQuerySizeToLogValue
            val extractPosition: Int = if (truncated) maxQuerySizeToLogValue + queryPosition else oldPacketPosition
            val realSql = String(byteBuffer, queryPosition, (extractPosition - queryPosition))
            Monitor.saveRecord(realSql, startTime, endTime);
        }

        @JvmStatic
        fun mysql5SqlHandle(queryPackage: Any?, startTime: Long) {
            if (queryPackage == null) return
            val endTime = System.currentTimeMillis() //执行结束时间
            val byteBuffer = getFieldValue("byteBuffer", queryPackage) as ByteArray
//            val realSql = String(byteBuffer, 5,byteBuffer.size-5)
            //出去 queryPackage多余的部分
            val realSql = getSql(byteBuffer.clone())
            Monitor.saveRecord(realSql, startTime, endTime)
        }

        /** 将packet byte 转换为 sql字符串 */
        private fun getSql(sqlByte: ByteArray): String {
            val byteData = sqlByte.clone()
            var beginIndex = -1
            var endIndex = -1
            for ((index, byte) in byteData.withIndex()) {
                val byteInt = byte.toInt()
                if (beginIndex == -1 && byte in 48..122) {
                    beginIndex = index
                }
                if (beginIndex != -1 && endIndex == -1 && byteInt == 0) {
                    endIndex = index
                }
                if (byteInt == 0x0A || byteInt == 0x09 || byteInt == 0x0D) {
                    byteData[index] = 0x20
                }
            }
            if (endIndex == -1) endIndex = byteData.size
            if (beginIndex == -1) beginIndex = 0
            return String(byteData, beginIndex, endIndex - beginIndex)
        }


        /**
         * 反射获取对象字段的值
         * TODO 放到工具类
         * @param name 属性名称
         * @param obj 要获取的对象
         * @return 属性的值
         */
        private fun getFieldValue(name: String, obj: Any): Any? {
            val field =
                getField(name, obj::class.java) ?: throw NullPointerException("获取属性 $name 失败,当前对象${objToString(obj)}")
            field.isAccessible = true
            return field.get(obj);
        }

        private fun getField(name: String, obj: Class<*>?): Field? {
            if (obj == null) return null
            return try {
                obj.getDeclaredField(name)
            } catch (e: Exception) {
                logger.debug { "从 ${obj.canonicalName} 获取属性 ${name} 失败，尝试从父类获取" }
                getField(name, obj.superclass)
            }
        }

        /**
         * 反射执行目标方法，并且返回方法执行结果
         * @param name 要执行的方法名称
         * @param obj 要执行方法的对象
         * @param args 方法执行的参数
         * @return 方法执行结果
         */
        private fun execMethod(name: String, obj: Any, args: Any?, vararg argTypes: Class<*>?): Any? {
            val method = getMethod(name, obj::class.java, *argTypes) ?: let {
                throw NullPointerException("获取方法 $name 失败,当前对象${objToString(obj)}")
            }
            method.isAccessible = true
            try {
                return if (args == null) {
                    method.invoke(obj)
                } else {
                    method.invoke(obj, args)
                }
            } catch (e: Exception) {
                logger.debug { "当前方法: $method " }
                throw e
            }

        }

        private fun getMethod(name: String, obj: Class<*>?, vararg argTypes: Class<*>?): Method? {
            if (obj == null) return null
            return try {
                if (argTypes.isEmpty() || argTypes[0] == null) {
                    obj.getMethod(name)
                } else {
                    obj.getMethod(name, *argTypes)
                }
            } catch (e: Exception) {
                logger.debug { "从 ${obj.canonicalName} 获取方法 ${name} 失败，尝试使用methods直接获取" }
                for (method in obj.methods) {
                    if (method.name.equals(name)) {
                        return method
                    }
                }
                logger.debug { "从 ${obj.canonicalName} 获取方法 ${name} 失败，尝试从父类获取" }
                getMethod(name, obj.superclass)
            }
        }

        private fun objToString(obj: Any?): String {
            return if (obj == null) {
                "null"
            } else {
                obj::class.java.canonicalName
            }
        }

    }
}

