package no.nav.tms.statistikk.eksternVarsling

import kotliquery.queryOf
import no.nav.tms.statistikk.database.Database
import java.time.LocalDateTime
import java.time.ZoneId


class EksternVarslingRepository(val db: Database) {

    fun insertEksternVarsling(eventId: String, kanal: String, ident: String) {

        val conflictString = when{
            kanal.erEpost() -> "DO UPDATE SET epost=true"
            kanal.erSms() -> "DO UPDATE SET sms=true"
            else -> "DO NOTHING"
        }

        db.update {
            queryOf(
                """INSERT INTO innlogging_etter_eksternt_varsel(eventid,ident,dato,sendttimestamp,epost,sms) 
                |VALUES(:eventId,:ident,:now::date,:now,:epost,:sms)
                |ON CONFLICT(eventId,dato) 
                |$conflictString 
            """.trimMargin(),
                mapOf(
                    "eventId" to eventId,
                    "ident" to ident,
                    "now" to LocalDateTime.now(ZoneId.of("UTC")),
                    "epost" to (kanal.erEpost()),
                    "sms" to (kanal.erSms())
                )
            )
        }
    }

    fun updateVarsel(eventId:String,kanal:String) = db.update {

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