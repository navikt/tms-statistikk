package no.nav.tms.statistikk.login

import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.routing.*

fun Route.loginApi(loginRepository: LoginRepository) {
    post("/innlogging") {
        val login: Login = call.receive()

        loginRepository.registerLogin(login.ident)
    }
}

private data class Login(val ident: String)
