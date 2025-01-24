package no.nav.tms.statistikk.login

import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.loginApi() {
    post("/innlogging") {
        call.respond(HttpStatusCode.NoContent)
    }
}
