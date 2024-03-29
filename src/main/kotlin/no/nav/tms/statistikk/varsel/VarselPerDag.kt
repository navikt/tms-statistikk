package no.nav.tms.statistikk.varsel

import java.time.LocalDateTime

data class VarselPerDag(
    val ident: String,
    val type: String,
    val eksternVarsling: Boolean,
    val opprettet: LocalDateTime
)
