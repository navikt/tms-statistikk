package no.nav.tms.statistikk.varsel

import io.github.oshai.kotlinlogging.KotlinLogging
import no.nav.helse.rapids_rivers.*
import no.nav.tms.statistikk.asUtcDateTime

class VarselPerDagSink(
    rapidsConnection: RapidsConnection,
    private val repository: VarselRepository
): River.PacketListener {

    private val log = KotlinLogging.logger {}

    init {
        River(rapidsConnection).apply {
            validate {
                it.demandValue("@event_name", "opprettet")
                it.requireKey("ident", "type", "opprettet")
                it.interestedIn("eksternVarslingBestilling")
            }
        }.register(this)
    }

    override fun onPacket(packet: JsonMessage, context: MessageContext) {
        repository.registerVarselPerDag(deserializeVarsel(packet))
    }

    override fun onError(problems: MessageProblems, context: MessageContext) {
        log.info { problems.toString() }
    }

    private fun deserializeVarsel(jsonMessage: JsonMessage) = VarselPerDag(
        ident = jsonMessage["ident"].textValue(),
        type = jsonMessage["type"].textValue(),
        eksternVarsling = jsonMessage["eksternVarslingBestilling"].isMissingOrNull().not(),
        opprettet = jsonMessage["opprettet"].asUtcDateTime()
    )
}
