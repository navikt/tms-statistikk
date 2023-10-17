package no.nav.tms.statistikk.microfrontends

import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.MessageContext
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.helse.rapids_rivers.River

internal class MicrofrontendSink(
    rapidsConnection: RapidsConnection,
    private val microfrontendRepository: MicrofrontendRepository
) : River.PacketListener {
    init {
        River(rapidsConnection).apply {
            validate { it.requireKey("@action") }
            validate { it.interestedIn("ident", "microfrontend_id","@initiated_by") }
        }.register(this)

    }

    override fun onPacket(packet: JsonMessage, context: MessageContext) {
        microfrontendRepository.insertMicrofrontend(
            action=packet["@action"].asText(),
            microfrontendId = packet["microfrontend_id"].asText(),
            ident = packet["ident"].asText(),
            initiatedBy = packet["@initiated_by"].textValue()
        )
    }
}