package no.nav.tms.statistikk.varsel

import LocalPostgresDatabase
import kotliquery.queryOf
import java.time.LocalDate
import java.time.LocalDateTime

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
                 ${if (type != null) "and type = :type" else ""}
                 ${if (dato != null) "and dato = :dato" else ""}
                 ${if (eksternVarsling != null) "and ekstern_varsling = :eksternVarsling" else ""}
                 group by ident """,
            mapOf("ident" to ident, "type" to type?.name, "dato" to dato, "eksternVarsling" to eksternVarsling)
        ).map { it.int("totalt_antall") }.asSingle
    } ?: 0
}

fun LocalPostgresDatabase.getVarsel(eventId: String): DBAktivertVarsel? = query {
    queryOf("""select v.*, b.beredskap_tittel 
        |from varsel as v 
        |left join beredskapsvarsel as b on b.varselId=v.eventId
        |where eventId = :eventId""".trimMargin(), mapOf("eventId" to eventId))
        .map { row ->
            DBAktivertVarsel(
                varselId = row.string("eventId"),
                ident = row.string("ident"),
                type = row.string("type"),
                namespace = row.string("namespace"),
                appnavn = row.string("appnavn"),
                tekstlengde = row.int("tekstlengde"),
                lenke = row.boolean("lenke"),
                sikkerhetsnivaa = row.int("sikkerhetsnivaa"),
                aktiv = row.boolean("aktiv"),
                forstBehandlet = row.localDateTime("forstBehandlet"),
                frist = row.boolean("frist"),
                inaktivertTidspunkt = row.localDateTimeOrNull("inaktivertTidspunkt"),
                inaktivertKilde = row.stringOrNull("inaktivertKilde"),
                eksternVarslingBestilt = row.boolean("eksternVarslingBestilt"),
                eksternVarslingSendtSms = row.boolean("eksternVarslingSendtSms"),
                eksternVarslingSendtEpost = row.boolean("eksternVarslingSendtEpost"),
                beredskapstittel = row.stringOrNull("beredskap_tittel")
            )
        }.asSingle
}

data class DBAktivertVarsel(
    val varselId: String,
    val ident: String,
    val type: String,
    val namespace: String,
    val appnavn: String,
    val tekstlengde: Int,
    val lenke: Boolean,
    val sikkerhetsnivaa: Int,
    val aktiv: Boolean,
    val forstBehandlet: LocalDateTime,
    val frist: Boolean,
    val inaktivertTidspunkt: LocalDateTime?,
    val inaktivertKilde: String?,
    val eksternVarslingBestilt: Boolean,
    val eksternVarslingSendtSms: Boolean,
    val eksternVarslingSendtEpost: Boolean,
    val beredskapstittel: String?
)
