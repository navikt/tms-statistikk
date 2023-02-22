package no.nav.tms.statistikk

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import io.ktor.serialization.jackson.jackson
import io.ktor.server.application.*
import io.ktor.server.auth.authenticate
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.routing.routing
import no.nav.tms.statistikk.database.PostgresDatabase
import no.nav.tms.statistikk.login.LoginRepository
import no.nav.tms.statistikk.login.loginApi
import no.nav.tms.token.support.authentication.installer.installAuthenticators
import java.text.DateFormat

internal fun Application.statistikkApi(
    database: PostgresDatabase,
    installAuthenticatorsFunction: Application.() -> Unit = installAuth(),
) {
    installAuthenticatorsFunction()

    install(ContentNegotiation) {
        jackson {
            registerModule(JavaTimeModule())
            dateFormat = DateFormat.getDateTimeInstance()
        }
    }

    routing {
        authenticate {
            loginApi(LoginRepository(database))
        }
    }
}

private fun installAuth(): Application.() -> Unit = {
    installAuthenticators {
        installAzureAuth {
            setAsDefault = true
        }
    }
}
