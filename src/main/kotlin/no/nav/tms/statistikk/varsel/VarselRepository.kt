package no.nav.tms.statistikk.varsel

import kotliquery.queryOf
import no.nav.tms.statistikk.database.Database

class VarselRepository(private val database: Database) {
    fun insertVarsel(varsel: Varsel) = database.update {
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
                inaktivertTidspunkt,
                inaktivertKilde,
                eksternVarslingBestilt,
                eksternVarslingSendtSms,
                eksternVarslingSendtEpost
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
                :inaktivertTidspunkt,
                :inaktivertKilde,
                :eksternVarslingBestilt,
                :eksternVarslingSendtSms,
                :eksternVarslingSendtEpost
            ) on conflict do nothing
        """,
            mapOf(
                "eventId" to varsel.eventId,
                "ident" to varsel.ident,
                "type" to varsel.type,
                "namespace" to varsel.namespace,
                "appnavn" to varsel.appnavn,
                "tekstlengde" to varsel.tekstlengde,
                "lenke" to varsel.lenke,
                "sikkerhetsnivaa" to varsel.sikkerhetsnivaa,
                "aktiv" to varsel.aktiv,
                "frist" to varsel.frist,
                "forstBehandlet" to varsel.forstBehandlet,
                "inaktivertTidspunkt" to varsel.inaktivertTidspunkt,
                "inaktivertKilde" to varsel.inaktivertKilde,
                "eksternVarslingBestilt" to varsel.eksternVarslingBestilt,
                "eksternVarslingSendtSms" to varsel.eksternVarslingSendtSms,
                "eksternVarslingSendtEpost" to varsel.eksternVarslingSendtEpost
            )
        )
    }

    fun updateVarsel(varselInaktivert: VarselInaktivert) = database.update {
        queryOf("""
            update varsel set aktiv = false, inaktivertTidspunkt = :tidspunkt, inaktivertKilde = :kilde
            where eventId = :eventId
        """,
            mapOf(
                "eventId" to varselInaktivert.eventId,
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
                "dato" to varselPerDag.forstBehandlet.toLocalDate(),
                "ident" to varselPerDag.ident,
                "type" to varselPerDag.type,
                "eksternVarsling" to varselPerDag.eksternVarsling
            )
        )
    }
}
