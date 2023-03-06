package no.nav.tms.statistikk.api

import kotliquery.queryOf
import no.nav.tms.statistikk.database.Database
import java.time.LocalDate
import java.time.ZoneId

class InnloggingRepository(private val database: Database) {
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

    fun getInnloggingGjennomsnitt():Int= database.query {
        //language=PostgreSQL
        queryOf(
            """
                    select to_char(dato,'mm') as month,to_char(dato,'YYYY') as year, count(*) from innlogging_per_dag
                    group by month,year
                    order by month,year
                    """
        )
            .map {
                   it.int("count")
            }.asSingle
    } ?: throw StatistikkContentException("Fant ikke statistikk for innloggede brukere")

}