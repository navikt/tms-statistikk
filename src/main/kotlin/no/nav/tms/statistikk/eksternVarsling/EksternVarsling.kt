package no.nav.tms.statistikk.eksternVarsling

import no.nav.helse.rapids_rivers.JsonMessage

data class EksternVarslingSendt(
    val eventId: String,
    val kanal: Kanal
)

val JsonMessage.ident: String
    get() = get("ident").asText()
val JsonMessage.kanal: Kanal
    get() = Kanal.valueOf(get("kanal").asText().uppercase())
val JsonMessage.eventId: String
    get() = get("eventId").asText()

enum class Kanal {
    SMS, EPOST;
}
