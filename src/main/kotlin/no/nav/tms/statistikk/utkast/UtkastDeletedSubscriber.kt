package no.nav.tms.statistikk.utkast

import no.nav.tms.kafka.application.JsonMessage
import no.nav.tms.kafka.application.Subscriber
import no.nav.tms.kafka.application.Subscription

class UtkastDeletedSubscriber(
    val utkastRespository: UtkastRespository
) : Subscriber() {

    override fun subscribe() = Subscription.forEvent("deleted")
        .withFields("utkastId")

    override suspend fun receive(jsonMessage: JsonMessage) {
        utkastRespository.insertDeleted(
            utkastId = jsonMessage["utkastId"].asText(),
        )
    }
}
