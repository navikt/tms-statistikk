package no.nav.tms.statistikk.varsel

import java.time.LocalDateTime

data class Varsel(
    val ident: String,
    val type: String,
    val eksternVarsling: Boolean,
    val forstBehandlet: LocalDateTime
)
