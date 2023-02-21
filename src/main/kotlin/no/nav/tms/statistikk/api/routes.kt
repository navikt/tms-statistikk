package no.nav.tms.statistikk.api

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

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

interface StatistikkPersistence {
    fun updateLoginCount(ident:String)
    fun getCSV(): String
}

data class InnloggingRequestBody(val ident: String)