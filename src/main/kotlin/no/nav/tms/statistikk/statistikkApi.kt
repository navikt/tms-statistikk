package no.nav.tms.statistikk

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import io.ktor.serialization.jackson.jackson
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.routing.routing
import io.micrometer.prometheus.PrometheusMeterRegistry
import no.nav.tms.statistikk.api.StatistikkPersistence
import no.nav.tms.statistikk.api.statistikk
import no.nav.tms.statistikk.metrics.metrics
import java.text.DateFormat

internal fun Application.statistikkApi(
    prometheusMeterRegistry: PrometheusMeterRegistry,
    persistence: StatistikkPersistence
) {
    install(ContentNegotiation) {
        jackson {
            registerModule(JavaTimeModule())
            dateFormat = DateFormat.getDateTimeInstance()
        }
    }


    routing {
        metrics(prometheusMeterRegistry)
        statistikk(persistence)
    }
}
