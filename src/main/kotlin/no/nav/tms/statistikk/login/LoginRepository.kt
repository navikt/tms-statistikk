package no.nav.tms.statistikk.login

import kotliquery.queryOf
import no.nav.tms.statistikk.database.Database
import java.time.LocalDate
import java.time.LocalDateTime

class LoginRepository(private val database: Database) {
    fun registerLogin(ident: String) = database.updateReturningCount {
        queryOf(
            "insert into innlogging_per_dag(dato, ident) values (:dato, :ident) on conflict do nothing",
            mapOf(
                "dato" to LocalDate.now(),
                "ident" to ident
            )
        )
    }.also {
        if (it > 0) {
            database.update {
                queryOf(
                    """update innlogging_etter_eksternt_varsel
                        set innloggetTimestamp=:nowTime
                        where ident=:ident and innloggetTimestamp is null
                """.trimIndent(),
                    mapOf(
                        "ident" to ident,
                        "nowTime" to LocalDateTime.now(),
                        "nowDate" to LocalDate.now()
                    )
                )
            }
        }
    }

    fun `innlogging samme dag etter ekstern varsling`(): Int = database.query {
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
    } ?: 0
}
