package no.nav.tms.statistikk.varsel

import java.time.LocalDateTime
import java.time.LocalDateTime.now

object VarselTestData {
    enum class VarselType { beskjed, oppgave, innboks }

    fun varselAktivertMessage(ident: String, type: VarselType, eksternVarsling: Boolean = false, forstBehandlet: LocalDateTime = now()) =
    """
        {
            "@event_name": "aktivert",
            "fodselsnummer": "$ident",
            "varselType": "${type.name}",
            "eksternVarsling": $eksternVarsling,
            "forstBehandlet": "$forstBehandlet"
        }
    """.trimIndent()
}
