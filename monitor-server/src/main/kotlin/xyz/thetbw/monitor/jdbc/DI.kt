package xyz.thetbw.monitor.jdbc

import org.kodein.di.DI
import org.kodein.di.bind
import org.kodein.di.singleton
import xyz.thetbw.monitor.jdbc.service.AgentService
import xyz.thetbw.monitor.jdbc.service.LogService

val di = DI{
    bind<AgentService>() with singleton { AgentService() }
    bind<LogService>() with singleton { LogService() }
}