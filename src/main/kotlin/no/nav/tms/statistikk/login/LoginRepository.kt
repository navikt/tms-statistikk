package no.nav.tms.statistikk.login

import kotliquery.queryOf
import no.nav.tms.statistikk.database.Database
import java.time.LocalDate

class LoginRepository(private val database: Database) {
    fun registerLogin(ident: String) = database.update {
        queryOf(
            "insert into innlogging_per_dag(dato, ident) values (:dato, :ident) on conflict do nothing",
            mapOf(
                "dato" to LocalDate.now(),
                "ident" to ident
            )
        )
    }
}
