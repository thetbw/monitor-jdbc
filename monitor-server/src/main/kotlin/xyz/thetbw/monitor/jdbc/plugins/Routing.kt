package xyz.thetbw.monitor.jdbc.plugins

import io.ktor.application.*
import io.ktor.features.*
import io.ktor.http.content.*
import io.ktor.response.*
import io.ktor.routing.*
import xyz.thetbw.monitor.jdbc.ApiResult
import xyz.thetbw.monitor.jdbc.api.mainApi

fun Application.configureRouting() {

    install(CORS){
        host("*")
    }

    install(StatusPages){
        exception<Throwable> {
            it.printStackTrace()
            val message = it.message ?: "接口异常"
            call.respond(ApiResult<String>(false,message))
        }
    }

    install(Routing){
        route("/api"){
            mainApi();
        }
        get("/") {
            call.respondRedirect("/index.html")
        }
        get ("/index"){
            call.respondRedirect("/index.html")
        }
        // Static feature. Try to access `/static/index.html`
        static("/") {
            resources("static")
        }
    }
}
