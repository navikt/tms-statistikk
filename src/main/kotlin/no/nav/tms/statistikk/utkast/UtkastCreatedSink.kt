package no.nav.tms.statistikk.utkast

import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.MessageContext
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.helse.rapids_rivers.River

internal class UtkastCreatedSink(
    rapidsConnection: RapidsConnection,
    val utkastRespository: UtkastRespository
) : River.PacketListener {
    init {
        River(rapidsConnection).apply {
            validate { it.requireValue("@event_name", "created") }
            validate { it.requireKey("utkastId") }
            validate { it.interestedIn("ident", "utkastId", "tittel_i18n") }
        }.register(this)

    }

    override fun onPacket(packet: JsonMessage, context: MessageContext) {
        utkastRespository.insertCreated(
            utkastId = packet["utkastId"].asText(),
            ident = packet["ident"].asText(),
            antallSpråk = packet.antallSpråk()
        )
    }

}

private fun JsonMessage.antallSpråk(): Int =
    this["tittel_i18n"].toList().size.let {
        if(it == 0)
            1
        else it
    }