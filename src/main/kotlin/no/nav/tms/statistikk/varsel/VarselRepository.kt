package no.nav.tms.statistikk.varsel

import kotliquery.queryOf
import no.nav.tms.statistikk.database.Database
import no.nav.tms.statistikk.toUtcLocalDateTime

class VarselRepository(private val database: Database) {
    fun insertVarsel(varsel: AktivertVarsel) = database.update {
        queryOf("""
            insert into varsel(
                eventId,
                ident,
                type,
                namespace,
                appnavn,
                tekstlengde,
                lenke,
                sikkerhetsnivaa,
                aktiv,
                frist,
                forstBehandlet,
                eksternVarslingBestilt
            ) values (
                :eventId,
                :ident,
                :type,
                :namespace,
                :appnavn,
                :tekstlengde,
                :lenke,
                :sikkerhetsnivaa,
                :aktiv,
                :frist,
                :forstBehandlet,
                :eksternVarslingBestilt
            ) on conflict do nothing
        """,
            mapOf(
                "eventId" to varsel.varselId,
                "ident" to varsel.ident,
                "type" to varsel.type,
                "namespace" to varsel.produsent.namespace,
                "appnavn" to varsel.produsent.appnavn,
                "tekstlengde" to varsel.tekstLengde,
                "lenke" to varsel.harLenke,
                "sikkerhetsnivaa" to varsel.sensitivitet.loginLevel,
                "aktiv" to true,
                "frist" to varsel.frist,
                "forstBehandlet" to varsel.opprettet.toUtcLocalDateTime(),
                "eksternVarslingBestilt" to varsel.eksternVarslingBestilt
            )
        )
    }

    fun updateVarsel(varselInaktivert: VarselInaktivert) = database.update {
        queryOf("""
            update varsel set aktiv = false, inaktivertTidspunkt = :tidspunkt, inaktivertKilde = :kilde
            where eventId = :eventId
        """,
            mapOf(
                "eventId" to varselInaktivert.varselId,
                "tidspunkt" to varselInaktivert.tidspunkt,
                "kilde" to varselInaktivert.kilde,
            )
        )
    }

    fun registerVarselPerDag(varselPerDag: VarselPerDag) = database.update {
            queryOf("""
                insert into varsler_per_dag(dato, ident, type, ekstern_varsling, antall)
                    values(:dato, :ident, :type, :eksternVarsling, 1)
                on conflict (dato, ident, type, ekstern_varsling) do update set antall = varsler_per_dag.antall + 1
            """,
            mapOf(
                "dato" to varselPerDag.opprettet.toLocalDate(),
                "ident" to varselPerDag.ident,
                "type" to varselPerDag.type,
                "eksternVarsling" to varselPerDag.eksternVarsling
            )
        )
    }
}
