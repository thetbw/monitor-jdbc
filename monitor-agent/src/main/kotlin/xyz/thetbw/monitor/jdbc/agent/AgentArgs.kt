package xyz.thetbw.monitor.jdbc.agent

import com.xenomachina.argparser.ArgParser
import com.xenomachina.argparser.default

/**
 * agent 启动参数
 */
class AgentArgs(parser: ArgParser) {

    val debug by parser.flagging(
        "-d", "--debug",
        help = "enable debug mode"
    ).default { false }
}