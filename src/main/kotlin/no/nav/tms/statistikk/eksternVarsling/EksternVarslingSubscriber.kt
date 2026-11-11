package no.nav.tms.statistikk.eksternVarsling

import no.nav.tms.kafka.application.JsonMessage
import no.nav.tms.kafka.application.Subscriber
import no.nav.tms.kafka.application.Subscription


class EksternVarslingSubscriber(
    private val eksternVarslingRepository: EksternVarslingRepository
) : Subscriber() {

    override fun subscribe() = Subscription.forEvents("eksternStatusOppdatert", "eksternVarslingStatusOppdatert")
        .withFields(
            "kanal",
            "varselId",
            "ident",
            "tidspunkt"
        )
        .withValue("status", "sendt")

    override suspend fun receive(jsonMessage: JsonMessage) {
        eksternVarslingRepository.updateVarsel(jsonMessage.varselId, jsonMessage.kanal)
    }
}

val JsonMessage.kanal: String
    get() = get("kanal").asText()
val JsonMessage.varselId: String
    get() = get("varselId").asText()
