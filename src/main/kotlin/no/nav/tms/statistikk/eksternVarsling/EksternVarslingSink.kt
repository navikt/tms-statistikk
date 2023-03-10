package no.nav.tms.statistikk.eksternVarsling

import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.MessageContext
import no.nav.helse.rapids_rivers.MessageProblems
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
                it.requireKey("kanal", "eventId","ident")
            }
        }.register(this)
    }

    override fun onPacket(packet: JsonMessage, context: MessageContext) {
        eksternVarslingRepository.insertEksternVarsling(packet.eventId, packet.kanal, packet.ident)
    }

    override fun onError(problems: MessageProblems, context: MessageContext) {
        println("fant feil")
    }

    override fun onSevere(error: MessageProblems.MessageException, context: MessageContext) {
        println("fant feil")
    }


}

enum class Kanal {
    SMS, EPOST;
}

private val JsonMessage.ident: String
    get() = get("ident").asText()
private val JsonMessage.kanal: Kanal
    get() = Kanal.valueOf(get("kanal").asText().uppercase())
private val JsonMessage.eventId: String
    get() = get("eventId").asText()


