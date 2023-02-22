package no.nav.tms.statistikk.varsel

import kotliquery.queryOf
import no.nav.tms.statistikk.database.Database

class VarselRepository(private val database: Database) {
    fun registerVarselPerDag(varsel: Varsel) = database.update {
            queryOf("""
                insert into varsler_per_dag(dato, ident, type, ekstern_varsling, antall)
                    values(:dato, :ident, :type, :eksternVarsling, 1)
                on conflict do update set antall = antall + 1
            """,
            mapOf(
                "dato" to varsel.forstBehandlet.toLocalDate().toString(),
                "ident" to varsel.ident,
                "type" to varsel.type,
                "eksternVarsling" to varsel.eksternVarsling
            )
        )
    }
}
