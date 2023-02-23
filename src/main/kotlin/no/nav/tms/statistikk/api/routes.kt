package no.nav.tms.statistikk.api

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.html.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotliquery.queryOf
import no.nav.tms.statistikk.api.Statistikk.Companion.buildStats
import no.nav.tms.statistikk.database.Database
import java.time.LocalDate
import java.time.ZoneId


internal fun Routing.statistikk(persitance: StatistikkPersistence) {
    route("/innlogging") {
        post {
            persitance.updateLoginCount(call.receive<InnloggingRequestBody>().ident)
            call.respond(HttpStatusCode.NoContent)
        }
    }

    route("/hent") {

        get {
            call.respondHtml(HttpStatusCode.OK) { buildStats(persitance.getStatistikk()) }
        }

        get("/lastned") {
            call.response.header("Content-Disposition", "attachment; filename=\"stats.csv\"")
            call.response.header("Content-Type", "text/csv")
            call.respondOutputStream {
                writeCsv(persitance.getStatistikk())
            }
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

    fun getStatistikk(): Statistikk? =
        database.query {
            //language=PostgreSQL
            queryOf(
                """
                    select to_char(dato,'mm') as month,to_char(dato,'YYYY') as year, count(*) from innlogging_per_dag
                    group by month,year
                    order by month,year
                    """
            )
                .map {
                    Statistikk(
                        innlogginger_per_dag = it.int("count"),
                        måned = it.string("month"),
                        år = it.string("year")
                    )
                }.asSingle
        }
}

data class InnloggingRequestBody(val ident: String)

