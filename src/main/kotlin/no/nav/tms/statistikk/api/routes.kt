package no.nav.tms.statistikk.api

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.html.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.html.a
import kotlinx.html.body
import kotlinx.html.h1
import kotlinx.html.head
import kotlinx.html.title
import kotliquery.queryOf
import no.nav.tms.statistikk.database.Database
import java.io.OutputStream
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

        get{

            val lastNedUrl = "/hent/lastned"

            call.respondHtml(HttpStatusCode.OK) {
                head {
                    title("Min side stats")
                }
                body {
                    h1("Backend statistikk for min side")
                    a{
                        href = lastNedUrl
                        text("Last ned CSV-fil")
                    }

                }
            }

        }

        get("/lastned") {
            call.response.header("Content-Disposition", "attachment; filename=\"stats.csv\"")
            call.response.header("Content-Type","text/csv")
            call.respondOutputStream {
                writeCsv(persitance.getCSV())
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
    fun getCSV(): CSVContent? =
        database.query {
            queryOf("SELECT COUNT(ident) as total FROM innlogging_per_dag")
                .map {
                    CSVContent(it.int("total"))
                }.asSingle
        }
}

data class InnloggingRequestBody(val ident: String)

data class CSVContent(val innlogginger_per_dag: Int)

private fun OutputStream.writeCsv(csvContent: CSVContent?) {
    require(csvContent!=null)
    val writer = bufferedWriter()
    writer.write(""" Måned,Gjennomsnitt innloggede pr dag""")
    writer.newLine()
    writer.write(""" Februar, ${csvContent.innlogginger_per_dag} """)
    writer.flush()
}