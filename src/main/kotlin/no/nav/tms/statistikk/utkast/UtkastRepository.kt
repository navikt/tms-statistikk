package no.nav.tms.statistikk.utkast

import kotliquery.queryOf
import no.nav.tms.common.postgres.PostgresDatabase
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

class UtkastRespository(val database: PostgresDatabase) {
    fun insertCreated(utkastId: String, ident: String, antallSpråk: Int) {
        database.update {
            //language=PostgreSQL
            queryOf(
                """INSERT INTO utkast(ident, utkast_id, dato, event, time_created, antall_språk) 
                    VALUES(:ident,:utkastId,:dato,'created',:timestamp,:antallSprak) 
                    ON CONFLICT DO NOTHING 
                """,
                mapOf(
                    "ident" to ident,
                    "utkastId" to utkastId,
                    "dato" to LocalDate.now(ZoneId.of("UTC")),
                    "timestamp" to LocalDateTime.now(ZoneId.of("UTC")),
                    "antallSprak" to antallSpråk
                )
            )
        }
    }

    fun insertDeleted(utkastId: String) {
        database.update {
            //language=PostgreSQL
            queryOf(
                """UPDATE utkast 
                    SET event='deleted',
                    dato =:dato,
                    time_deleted=:timestamp 
                    WHERE utkast_id=:utkastId AND event!='deleted'
                    """,
                mapOf(
                    "utkastId" to utkastId,
                    "dato" to LocalDate.now(ZoneId.of("UTC")),
                    "timestamp" to LocalDateTime.now(ZoneId.of("UTC")),
                )
            )
        }
    }
}
