package no.nav.tms.statistikk.eksternVarsling

import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.MessageContext
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.helse.rapids_rivers.River
import no.nav.tms.statistikk.asUtcDateTime
import java.time.LocalDateTime


class EksternVarslingSink(
    rapidsConnection: RapidsConnection,
    val eksternVarslingRepository: EksternVarslingRepository
) : River.PacketListener {

    init {
        River(rapidsConnection).apply {
            validate {
                it.demandValue("@event_name", "eksternStatusOppdatert")
                it.demandValue("@source", "varsel-authority")
                it.demandValue("status", "sendt")
                it.requireKey("kanal", "varselId", "ident", "tidspunkt")
            }
        }.register(this)
    }

    override fun onPacket(packet: JsonMessage, context: MessageContext) {
        eksternVarslingRepository.insertEksternVarsling(packet.varselId, packet.kanal, packet.ident, packet.tidspunkt)
        eksternVarslingRepository.updateVarsel(packet.varselId,packet.kanal)
    }
}

val JsonMessage.ident: String
    get() = get("ident").asText()
val JsonMessage.kanal: String
    get() = get("kanal").asText()
val JsonMessage.varselId: String
    get() = get("varselId").asText()
val JsonMessage.tidspunkt: LocalDateTime
    get() = get("tidspunkt").asUtcDateTime()




