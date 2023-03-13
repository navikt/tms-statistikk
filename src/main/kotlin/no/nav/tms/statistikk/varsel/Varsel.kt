package no.nav.tms.statistikk.varsel

import java.time.LocalDateTime

data class Varsel(
    val eventId: String,
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
)

data class VarselInaktivert(
    val eventId: String,
    val tidspunkt: LocalDateTime?,
    val kilde: String
)
