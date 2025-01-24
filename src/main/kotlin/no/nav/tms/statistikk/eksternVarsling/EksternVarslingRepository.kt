package no.nav.tms.statistikk.eksternVarsling

import kotliquery.queryOf
import no.nav.tms.statistikk.database.Database
import java.time.LocalDateTime
import java.time.ZonedDateTime


class EksternVarslingRepository(val db: Database) {

    fun updateVarsel(eventId: String, kanal: String) = db.update {

        queryOf(
            """
            update varsel set 
                eksternVarslingSendtSms = (eksternVarslingSendtSms or :sendtSms),
                eksternVarslingSendtEpost = (eksternVarslingSendtEpost or :sendtEpost)
            where eventId = :eventId
        """,
            mapOf(
                "eventId" to eventId,
                "sendtSms" to (kanal.erSms()),
                "sendtEpost" to (kanal.erEpost())

            )
        )
    }
}

private fun String.erSms() = lowercase() == "sms"
private fun String.erEpost() = lowercase() == "epost"
