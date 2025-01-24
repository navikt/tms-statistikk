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
import no.nav.tms.statistikk.login.loginApi
import no.nav.tms.token.support.azure.validation.azure
import java.io.IOException
import java.text.DateFormat

fun Application.statistikkApi(
    installAuthenticatorsFunction: Application.() -> Unit = installAuth(),
) {
    installAuthenticatorsFunction()

    install(StatusPages) {
        val log = KotlinLogging.logger {}
        val securelog = KotlinLogging.logger("secureLog")

        exception<Throwable> { call, cause ->
            when (cause) {
                is IllegalArgumentException -> {
                    log.warn { "Bad request til statistikkApi" }
                    securelog.warn(cause) { "Bad request til statistikkApi" }
                    call.respond(HttpStatusCode.BadRequest, cause.message ?: "")
                }
                is IOException -> {
                    log.warn { "IO-feil i statistikk-api" }
                    securelog.warn(cause) { "IO-feil i statistikk-api" }
                    call.respond(HttpStatusCode.InternalServerError)
                }
                else -> {
                    log.error { "Feil i statistikkApi" }
                    securelog.error(cause) { "Feil i statistikkApi" }
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
            loginApi()
        }
    }
}

fun installAuth(): Application.() -> Unit = {
    authentication {
        azure {
            setAsDefault = true
        }
    }
}
