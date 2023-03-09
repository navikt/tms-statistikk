package no.nav.tms.statistikk.varsel

import com.fasterxml.jackson.databind.JsonNode
import mu.KotlinLogging
import no.nav.helse.rapids_rivers.*
import java.time.LocalDateTime

class VarselInaktivertSink(
    rapidsConnection: RapidsConnection,
    private val repository: VarselRepository
): River.PacketListener {

    private val log = KotlinLogging.logger {}

    init {
        River(rapidsConnection).apply {
            validate { it.demandValue("@event_name", "inaktivert") }
            validate { it.requireKey(
                "eventId",
                "kilde"
            )}
            validate { it.interestedIn("tidspunkt") }
        }.register(this)
    }

    override fun onPacket(packet: JsonMessage, context: MessageContext) {
        repository.updateVarsel(deserializeVarselInaktivert(packet))
    }

    override fun onError(problems: MessageProblems, context: MessageContext) {
        log.info(problems.toString())
    }

    private fun deserializeVarselInaktivert(json: JsonMessage) = VarselInaktivert(
        eventId = json["eventId"].textValue(),
        kilde = json["kilde"].textValue(),
        tidspunkt = json["tidspunkt"].asOptionalLocalDateTime()
    )
}
