package no.nav.tms.statistikk

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import io.ktor.serialization.jackson.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.routing.*
import no.nav.tms.statistikk.api.StatistikkPersistence
import no.nav.tms.statistikk.database.PostgresDatabase
import no.nav.tms.statistikk.login.LoginRepository
import no.nav.tms.statistikk.login.loginApi
import no.nav.tms.statistikk.api.statistikk
import java.text.DateFormat

internal fun Application.statistikkApi(
    database: PostgresDatabase,
    installAuthenticatorsFunction: Application.() -> Unit = installAuth(),
) {
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
        statistikk(StatistikkPersistence(database))
    }
}


