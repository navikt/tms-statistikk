package no.nav.tms.statistikk.varsel

import mu.KotlinLogging
import no.nav.helse.rapids_rivers.*

class VarselPerDagSink(
    rapidsConnection: RapidsConnection,
    private val repository: VarselRepository
): River.PacketListener {

    private val log = KotlinLogging.logger {}

    init {
        River(rapidsConnection).apply {
            validate { it.demandValue("@event_name", "aktivert") }
            validate { it.requireKey("fodselsnummer", "varselType", "forstBehandlet", "eksternVarsling") }
        }.register(this)
    }

    override fun onPacket(packet: JsonMessage, context: MessageContext) {
        repository.registerVarselPerDag(deserializeVarsel(packet))
    }

    override fun onError(problems: MessageProblems, context: MessageContext) {
        log.info(problems.toString())
    }

    private fun deserializeVarsel(jsonMessage: JsonMessage) = VarselPerDag(
        ident = jsonMessage["fodselsnummer"].textValue(),
        type = jsonMessage["varselType"].textValue(),
        eksternVarsling = jsonMessage["eksternVarsling"].booleanValue(),
        forstBehandlet = jsonMessage["forstBehandlet"].asLocalDateTime()
    )
}
