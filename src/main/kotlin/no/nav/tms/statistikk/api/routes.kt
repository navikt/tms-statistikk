package no.nav.tms.statistikk.api

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotliquery.queryOf
import no.nav.tms.statistikk.database.Database
import java.time.LocalDate
import java.time.ZoneId

internal fun Routing.statistikk(persitance: StatistikkPersistence) {
    route("/innlogging") {
        post {
            persitance.updateLoginCount(call.receive<InnloggingRequestBody>().ident)
            call.respond(HttpStatusCode.Created)
        }
    }

    route("/hent") {
        get {
            persitance.getCSV()
            call.respond(HttpStatusCode.OK)
        }
    }
}

internal class StatistikkPersistence(private val database: Database) {
    fun updateLoginCount(ident: String) {
        database.update {
            queryOf(
                "INSERT INTO innlogging_per_dag VALUES(:dag,:ident) ON CONFLICT DO NOTHING", mapOf(
                    "dag" to LocalDate.now(ZoneId.of("UTC")),
                    "ident" to ident
                )
            )
        }

    }

    fun getCSV(): String {
        return ""
    }
}

data class InnloggingRequestBody(val ident: String)