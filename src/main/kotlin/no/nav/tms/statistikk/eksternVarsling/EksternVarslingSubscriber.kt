package no.nav.tms.statistikk.eksternVarsling

import no.nav.tms.kafka.application.JsonMessage
import no.nav.tms.kafka.application.Subscriber
import no.nav.tms.kafka.application.Subscription
import no.nav.tms.statistikk.asUtcDateTime
import java.time.LocalDateTime


class EksternVarslingSubscriber(
    private val eksternVarslingRepository: EksternVarslingRepository
) : Subscriber() {

    override fun subscribe() = Subscription.forEvent("eksternStatusOppdatert")
        .withFields(
            "kanal",
            "varselId",
            "ident",
            "tidspunkt"
        )
        .withValue("status", "sendt")

    override suspend fun receive(jsonMessage: JsonMessage) {
        eksternVarslingRepository.insertEksternVarsling(
            eventId = jsonMessage.varselId,
            kanal = jsonMessage.kanal,
            ident = jsonMessage.ident,
            tidspunkt = jsonMessage.tidspunkt
        )
        eksternVarslingRepository.updateVarsel(jsonMessage.varselId, jsonMessage.kanal)
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




