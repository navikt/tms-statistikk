package no.nav.tms.statistikk.varsel

import no.nav.tms.kafka.application.JsonMessage
import no.nav.tms.kafka.application.Subscriber
import no.nav.tms.kafka.application.Subscription
import no.nav.tms.kafka.application.isMissingOrNull
import no.nav.tms.statistikk.asUtcDateTime

class VarselPerDagSubscriber(
    private val repository: VarselRepository
): Subscriber() {

    override fun subscribe() = Subscription.forEvent("opprettet")
        .withFields(
            "ident",
            "type",
            "opprettet"
        )
        .withOptionalFields(
            "eksternVarslingBestilling"
        )

    override suspend fun receive(jsonMessage: JsonMessage) {
        val varselPerDag = VarselPerDag(
            ident = jsonMessage["ident"].textValue(),
            type = jsonMessage["type"].textValue(),
            eksternVarsling = jsonMessage.getOrNull("eksternVarslingBestilling").isMissingOrNull().not(),
            opprettet = jsonMessage["opprettet"].asUtcDateTime()
        )

        repository.registerVarselPerDag(varselPerDag)
    }
}
