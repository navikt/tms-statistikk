package no.nav.tms.statistikk.api

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.html.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import no.nav.tms.statistikk.login.LoginRepository


fun Routing.statistikk(loginRepository: LoginRepository) {
    route("/hent") {

        get {
            call.respondHtml(HttpStatusCode.OK) { buildStats() }
        }

        get("/innlogging") {
            call.response.header("Content-Disposition", "attachment; filename=\"innlogging.csv\"")
            call.response.header("Content-Type", "text/csv")
            call.respondOutputStream {
                writeInnloggingCSV(loginRepository.`innlogging samme dag etter ekstern varsling`())
            }
        }

        get("/lastned/utkast") {
            call.response.header("Content-Disposition", "attachment; filename=\"utkast.csv\"")
            call.response.header("Content-Type", "text/csv")
            call.respondOutputStream {
                write(5)
            }
        }
    }
}
