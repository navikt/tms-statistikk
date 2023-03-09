package no.nav.tms.statistikk.varsel

import com.fasterxml.jackson.databind.JsonNode
import mu.KotlinLogging
import no.nav.helse.rapids_rivers.*

class EksternVarslingSendtSink(
    rapidsConnection: RapidsConnection,
    private val repository: VarselRepository
): River.PacketListener {

    private val log = KotlinLogging.logger {}

    init {
        River(rapidsConnection).apply {
            validate { it.demandValue("@event_name", "eksternStatusOppdatert") }
            validate { it.demandValue("status", "sendt") }
            validate { it.requireKey(
                "eventId",
                "kanal"
            )}
        }.register(this)
    }

    override fun onPacket(packet: JsonMessage, context: MessageContext) {
        repository.updateVarsel(deserializeEksternVarslingSendt(packet))
    }

    override fun onError(problems: MessageProblems, context: MessageContext) {
        log.info(problems.toString())
    }

    private fun deserializeEksternVarslingSendt(json: JsonMessage) = EksternVarslingSendt(
        eventId = json["eventId"].textValue(),
        kanal = json["kanal"].textValue()
    )
}
