package no.nav.tms.statistikk.varsel

import java.time.LocalDateTime
import java.time.LocalDateTime.now

object VarselTestData {
    enum class VarselType { beskjed, oppgave, innboks }

    fun varselAktivertMessage(ident: String, type: VarselType, eksternVarsling: Boolean = false, tidspunkt: LocalDateTime = now()) =
    """
        {
            "@event_name": "aktivert",
            "fodselsnummer": "$ident",
            "eventType": "${type.name}",
            "eksternVarling": $eksternVarsling,
            "forstBehandlet": "$tidspunkt"
        }
    """.trimIndent()
}
