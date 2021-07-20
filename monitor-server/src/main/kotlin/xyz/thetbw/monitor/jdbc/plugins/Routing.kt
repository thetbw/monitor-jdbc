package xyz.thetbw.monitor.jdbc.plugins

import io.ktor.routing.*
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.application.*
import xyz.thetbw.monitor.jdbc.api.mainApi

fun Application.configureRouting() {

    install(Routing){
        route("/api"){
            mainApi();
        }
        // Static feature. Try to access `/static/index.html`
        static("/") {
            resources("static")
        }
    }
}
