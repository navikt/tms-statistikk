package no.nav.tms.statistikk

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import io.ktor.serialization.jackson.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.routing.*
import no.nav.tms.statistikk.api.StatistikkPersistence
import no.nav.tms.statistikk.api.statistikk
import java.text.DateFormat

internal fun Application.statistikkApi(
    persistence: StatistikkPersistence
) {
    install(ContentNegotiation) {
        jackson {
            registerModule(JavaTimeModule())
            dateFormat = DateFormat.getDateTimeInstance()
        }
    }
    routing {
        statistikk(persistence)
    }
}
