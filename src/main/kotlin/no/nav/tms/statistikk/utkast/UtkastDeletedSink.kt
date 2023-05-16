package no.nav.tms.statistikk.utkast

import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.MessageContext
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.helse.rapids_rivers.River

internal class UtkastDeletedSink(
    rapidsConnection: RapidsConnection,
    val utkastRespository: UtkastRespository
) : River.PacketListener {
    init {
        River(rapidsConnection).apply {
            validate { it.requireValue("@event_name", "deleted") }
            validate { it.requireKey("utkastId") }
        }.register(this)

    }

    override fun onPacket(packet: JsonMessage, context: MessageContext) {
        utkastRespository.insertDeleted(
            utkastId = packet["utkastId"].asText(),
        )
    }
}