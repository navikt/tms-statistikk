package no.nav.tms.statistikk.varsel

import LocalPostgresDatabase
import kotliquery.queryOf
import no.nav.tms.statistikk.database.PostgresDatabase
import java.time.LocalDate

fun LocalPostgresDatabase.antallVarsler(
    ident: String,
    type: VarselTestData.VarselType? = null,
    dato: LocalDate? = null,
    eksternVarsling: Boolean? = null
): Int {
    return query {
        queryOf(
            """select sum(antall) as totalt_antall from varsler_per_dag 
               where 
                 ident = :ident 
                 ${if(type != null) "and type = :type" else "" }
                 ${if(dato != null) "and dato = :dato" else "" }
                 ${if(eksternVarsling != null) "and ekstern_varsling = :eksternVarsling" else "" }
                 group by ident """,
            mapOf("ident" to ident, "type" to type?.name, "dato" to dato, "eksternVarsling" to eksternVarsling)
        ).map { it.int("totalt_antall") }.asSingle
    } ?: 0
}
