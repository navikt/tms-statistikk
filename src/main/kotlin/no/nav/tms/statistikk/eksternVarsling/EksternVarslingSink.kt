package no.nav.tms.statistikk.eksternVarsling

import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.MessageContext
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.helse.rapids_rivers.River


class EksternVarslingSink(
    rapidsConnection: RapidsConnection,
    val eksternVarslingRepository: EksternVarslingRepository
) : River.PacketListener {

    init {
        River(rapidsConnection).apply {
            validate {
                it.requireValue("@event_name", "eksternStatusOppdatert")
                it.requireValue("status", "sendt")
                it.requireKey("kanal", "eventId", "ident")
            }
        }.register(this)
    }

    override fun onPacket(packet: JsonMessage, context: MessageContext) {
        eksternVarslingRepository.insertEksternVarsling(packet.eventId, packet.kanal, packet.ident)
        eksternVarslingRepository.updateVarsel(deserializeEksternVarslingSendt(packet))
    }

    private fun deserializeEksternVarslingSendt(json: JsonMessage) = EksternVarslingSendt(
        eventId = json["eventId"].textValue(),
        kanal = Kanal.valueOf(json["kanal"].textValue())
    )
}


