package xyz.thetbw.monitor.jdbc.agent.transformers

import java.lang.instrument.ClassFileTransformer
import java.security.ProtectionDomain

abstract class JdbcTransformer: ClassFileTransformer {

    /**
     * 检查当前驱动是否支持
     * @param driveName 驱动名称
     * @param majorVersion 驱动大版本号
     * @param minorVersion 驱动小版本号
     * @return 当前 转换器是否支持 传入的驱动信息
     */
    abstract fun isSupport(driveName: String,majorVersion: Int,minorVersion: Int): Boolean

    /**
     * 当前转换器需要重新 住换的 class名称
     */
    abstract fun needTransClassesName(): Set<String>

    /**
     * 开始转换
     */
    abstract fun transform(className: String,originClassBuffer: ByteArray): ByteArray

    override fun transform(
        loader: ClassLoader?,
        className: String?,
        classBeingRedefined: Class<*>?,
        protectionDomain: ProtectionDomain?,
        classfileBuffer: ByteArray?
    ): ByteArray {
        className!!
        classfileBuffer!!
        val normalClassName = className.replace("/",".")
        if (needTransClassesName().contains(normalClassName)){
            return transform(normalClassName,classfileBuffer)
        }
        return classfileBuffer
    }







}