package no.nav.tms.statistikk.utkast

import no.nav.tms.kafka.application.JsonMessage
import no.nav.tms.kafka.application.Subscriber
import no.nav.tms.kafka.application.Subscription

class UtkastCreatedSubscriber(
    val utkastRespository: UtkastRespository
) : Subscriber() {
    override fun subscribe() = Subscription.forEvent("created")
        .withFields("utkastId", "ident")
        .withOptionalFields("tittel_i18n")


    override suspend fun receive(jsonMessage: JsonMessage) {
        utkastRespository.insertCreated(
            utkastId = jsonMessage["utkastId"].asText(),
            ident = jsonMessage["ident"].asText(),
            antallSpråk = jsonMessage.antallSpråk()
        )
    }

}

private fun JsonMessage.antallSpråk(): Int {
    val i18n = getOrNull("tittel_i18n")

    return if (i18n == null || i18n.size() == 0) {
        1
    } else {
        i18n.size()
    }
}
