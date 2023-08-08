package no.nav.tms.statistikk

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import io.ktor.http.*
import io.ktor.serialization.jackson.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.github.oshai.kotlinlogging.KotlinLogging
import no.nav.tms.statistikk.login.LoginRepository
import no.nav.tms.statistikk.login.loginApi
import no.nav.tms.statistikk.api.statistikk
import no.nav.tms.token.support.azure.validation.installAzureAuth
import java.text.DateFormat

fun Application.statistikkApi(
    loginRepository: LoginRepository,
    installAuthenticatorsFunction: Application.() -> Unit = installAuth(),
) {
    installAuthenticatorsFunction()

    install(StatusPages) {
        val log = KotlinLogging.logger {}
        exception<Throwable> { call, cause ->
            when (cause) {
                is IllegalArgumentException -> {
                    log.info { "Bad request til statistikkApi: $cause, ${cause.message.toString()}" }
                    call.respond(HttpStatusCode.BadRequest, cause.message ?: "")
                }
                else -> {
                    log.error { "Feil i statistikkApi: $cause, ${cause.message.toString()}" }
                    call.respond(HttpStatusCode.InternalServerError)
                }
            }
        }
    }

    install(ContentNegotiation) {
        jackson {
            registerModule(JavaTimeModule())
            dateFormat = DateFormat.getDateTimeInstance()
        }
    }
    routing {
        authenticate {
            loginApi(loginRepository)
        }
        statistikk(loginRepository)
    }
}

fun installAuth(): Application.() -> Unit = {
    installAzureAuth {
        setAsDefault = true
    }
}
