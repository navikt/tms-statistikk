package no.nav.tms.statistikk.microfrontends

import no.nav.tms.kafka.application.JsonMessage
import no.nav.tms.kafka.application.Subscriber
import no.nav.tms.kafka.application.Subscription

class MicrofrontendSubscriber(
    private val microfrontendRepository: MicrofrontendRepository
) : Subscriber() {

    override fun subscribe() = Subscription.forAllEvents()
        .withFields(
            "@action",
            "ident",
            "microfrontend_id"
        )
        .withOptionalFields("@initiated_by")


    override suspend fun receive(jsonMessage: JsonMessage) {
        microfrontendRepository.insertMicrofrontend(
            action = jsonMessage["@action"].asText(),
            microfrontendId = jsonMessage["microfrontend_id"].asText(),
            ident = jsonMessage["ident"].asText(),
            initiatedBy = jsonMessage.getOrNull("@initiated_by")?.textValue()
        )
    }
}
