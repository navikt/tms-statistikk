package no.nav.tms.statistikk.utkast

import kotliquery.queryOf
import no.nav.tms.statistikk.database.Database
import java.time.LocalDate
import java.time.ZoneId

internal class UtkastRespository(val database: Database) {
    fun put(utkastId: String, ident: String, event: String, antallSpråk: Int) {
        database.update {
            //language=PostgreSQL
            queryOf(
                """INSERT INTO utkast VALUES(:ident,:utkastId,:dato,:event,:antallSprak) 
                    |ON CONFLICT(utkast_id)
                    |DO UPDATE SET event=:event, dato=:dato, antall_språk=:antallSprak
                    |""".trimMargin(),
                mapOf(
                    "ident" to ident,
                    "utkastId" to utkastId,
                    "dato" to LocalDate.now(ZoneId.of("UTC")),
                    "event" to event,
                    "antallSprak" to antallSpråk
                )
            )
        }
    }
}