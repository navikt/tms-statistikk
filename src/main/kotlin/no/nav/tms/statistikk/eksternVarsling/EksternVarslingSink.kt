package no.nav.tms.statistikk.eksternVarsling

import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.MessageContext
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.helse.rapids_rivers.River
import no.nav.helse.rapids_rivers.asLocalDateTime
import java.time.LocalDateTime


class EksternVarslingSink(
    rapidsConnection: RapidsConnection,
    val eksternVarslingRepository: EksternVarslingRepository
) : River.PacketListener {

    init {
        River(rapidsConnection).apply {
            validate {
                it.requireValue("@event_name", "eksternStatusOppdatert")
                it.rejectValue("@source", "varsel-authority")
                it.requireValue("status", "sendt")
                it.requireKey("kanal", "eventId", "ident", "tidspunkt")
            }
        }.register(this)
    }

    override fun onPacket(packet: JsonMessage, context: MessageContext) {
        eksternVarslingRepository.insertEksternVarsling(packet.eventId, packet.kanal, packet.ident, packet.tidspunkt)
        eksternVarslingRepository.updateVarsel(packet.eventId,packet.kanal)
    }
}

val JsonMessage.ident: String
    get() = get("ident").asText()
val JsonMessage.kanal: String
    get() = get("kanal").asText()
val JsonMessage.eventId: String
    get() = get("eventId").asText()
val JsonMessage.tidspunkt: LocalDateTime
    get() = get("tidspunkt").asLocalDateTime()




