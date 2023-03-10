package no.nav.tms.statistikk.eksternVarsling

import kotliquery.queryOf
import no.nav.tms.statistikk.database.Database
import java.time.LocalDateTime


class EksternVarslingRepository(val db: Database) {

    fun insertEksternVarsling(eventId: String, kanal: Kanal, ident: String) {
        val params = mapOf(
            "eventId" to eventId,
            "ident" to ident,
            "now" to LocalDateTime.now(),
            "epost" to (kanal == Kanal.EPOST),
            "sms" to (kanal == Kanal.SMS)
        )

        val conflictString = if(kanal == Kanal.EPOST) {"epost=:epost"} else {"sms=:sms"}

        db.update {
            queryOf(
                """INSERT INTO innlogging_etter_eksternt_varsel(eventid,ident,dato,sendttimestamp,epost,sms) 
                |VALUES(:eventId,:ident,:now::date,:now,:epost,:sms)
                |ON CONFLICT(eventId,dato) 
                |DO UPDATE SET $conflictString 
            """.trimMargin(),
                params
            )
        }
    }

    fun updateVarsel(eksternVarslingSendt: EksternVarslingSendt) = db.update {

        val updateString =
            if(eksternVarslingSendt.kanal == Kanal.SMS) {"eksternVarslingSendtSms = true"}
            else {"eksternVarslingSendtEpost = true"}

        println("""
            update varsel set 
            $updateString    
            where eventId = :eventId
        """)

        queryOf("""
            update varsel set 
            $updateString    
            where eventId = :eventId
        """,
            mapOf(
                "eventId" to eksternVarslingSendt.eventId
            )
        )
    }
}
