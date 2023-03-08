package no.nav.tms.statistikk.utkast

import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.MessageContext
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.helse.rapids_rivers.River

internal class UtkastSink(
    rapidsConnection: RapidsConnection,
    val utkastRespository: UtkastRespository
) : River.PacketListener {
    init {
        River(rapidsConnection).apply {
            validate { it.demandAny("@event_name", listOf("created", "deleted")) }
            validate { it.requireKey("utkastId") }
            validate { it.interestedIn("ident", "utkastId", "tittel_i18n") }
        }.register(this)

    }

    override fun onPacket(packet: JsonMessage, context: MessageContext) {
        utkastRespository.put(
            utkastId = packet["utkastId"].asText(),
            ident = packet["ident"].asText(),
            event = packet["@event_name"].asText(),
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