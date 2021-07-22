package xyz.thetbw.monitor.jdbc

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

inline fun <reified T> T.toJson() = Json.encodeToString(this)

inline fun <reified T> String.fromJson() = Json.decodeFromString<T>(this)

fun error(msg: String){
    throw RuntimeException(msg)
}