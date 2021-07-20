package xyz.thetbw.monitor.jdbc.agent

import com.fasterxml.jackson.databind.ObjectMapper

fun Any.toJson() = ObjectMapper().writeValueAsString(this)

fun <T> String.fromJson(type: Class<T>) = ObjectMapper().readValue(this,type)