package no.nav.tms.statistikk.varsel

import no.nav.tms.kafka.application.JsonMessage
import no.nav.tms.kafka.application.Subscriber
import no.nav.tms.kafka.application.Subscription
import no.nav.tms.statistikk.asUtcDateTime

class VarselInaktivertSubscriber(
    private val repository: VarselRepository
): Subscriber() {

    override fun subscribe() = Subscription.forEvent("inaktivert")
        .withFields(
            "varselId",
            "kilde",
            "tidspunkt"
        )

    override suspend fun receive(jsonMessage: JsonMessage) {
        val inaktivertEvent = VarselInaktivert(
            varselId = jsonMessage["varselId"].textValue(),
            kilde = jsonMessage["kilde"].textValue(),
            tidspunkt = jsonMessage["tidspunkt"].asUtcDateTime()
        )

        repository.updateVarsel(inaktivertEvent)
    }
}
