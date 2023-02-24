package no.nav.tms.statistikk.api

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.html.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import no.nav.tms.statistikk.api.Statistikk.Companion.buildStats
import java.io.OutputStream


internal fun Routing.statistikk(persitance: InnloggingRepository) {
    route("/innlogging") {
        post {
            persitance.updateLoginCount(call.receive<InnloggingRequestBody>().ident)
            call.respond(HttpStatusCode.NoContent)
        }
    }

    route("/hent") {

        get {
            call.respondHtml(HttpStatusCode.OK) { buildStats(Statistikk(persitance.getInnloggingGjennomsnitt())) }
        }

        get("/lastned") {
            call.response.header("Content-Disposition", "attachment; filename=\"stats.csv\"")
            call.response.header("Content-Type", "text/csv")
            call.respondOutputStream {
                writeCsv(Statistikk(persitance.getInnloggingGjennomsnitt()))
            }
        }
    }
}


data class InnloggingRequestBody(val ident: String)

data class CSVContent(val innlogginger_per_dag: Int)

private fun OutputStream.writeCsv(csvContent: CSVContent?) {
    require(csvContent != null)
    val writer = bufferedWriter()
    writer.write("""Gjennomsnitt innloggede pr dag""")
    writer.newLine()
    writer.write("""${csvContent.innlogginger_per_dag} """)
    writer.flush()
}

