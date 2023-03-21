package no.nav.tms.statistikk.login

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.loginApi(loginRepository: LoginRepository) {
    post("/innlogging") {

            val login: Login = call.receive()
            loginRepository.registerLogin(login.ident)
            call.respond(HttpStatusCode.NoContent)
    }
}

private data class Login(val ident: String)
