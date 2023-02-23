package no.nav.tms.statistikk.varsel

import kotliquery.queryOf
import no.nav.tms.statistikk.database.PostgresDatabase

fun PostgresDatabase.antallVarsler(ident: String, type: VarselTestData.VarselType): Int {
    return query {
        queryOf(
            "select antall from varsler_per_dag where ident = :ident and type = :type",
            mapOf("ident" to ident, "type" to type.name)
        ).map { it.int("antall") }.asSingle
    } ?: 0
}
