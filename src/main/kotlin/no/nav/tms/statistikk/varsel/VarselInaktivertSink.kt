package no.nav.tms.statistikk.varsel

import io.github.oshai.kotlinlogging.KotlinLogging
import no.nav.helse.rapids_rivers.*
import no.nav.tms.statistikk.asOptionalUtcDateTime
import no.nav.tms.statistikk.asUtcDateTime

class VarselInaktivertSink(
    rapidsConnection: RapidsConnection,
    private val repository: VarselRepository
): River.PacketListener {

    private val log = KotlinLogging.logger {}

    init {
        River(rapidsConnection).apply {
            validate {
                it.demandValue("@event_name", "inaktivert")
                it.demandValue("@source", "varsel-authority")
                it.requireKey(
                    "varselId",
                    "kilde",
                    "tidspunkt"
                )
            }
        }.register(this)
    }

    override fun onPacket(packet: JsonMessage, context: MessageContext) {
        repository.updateVarsel(deserializeVarselInaktivert(packet))
    }

    override fun onError(problems: MessageProblems, context: MessageContext) {
        log.info { problems.toString() }
    }

    private fun deserializeVarselInaktivert(json: JsonMessage) = VarselInaktivert(
        varselId = json["varselId"].textValue(),
        kilde = json["kilde"].textValue(),
        tidspunkt = json["tidspunkt"].asUtcDateTime()
    )
}
