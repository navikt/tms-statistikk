package no.nav.tms.statistikk.varsel

import com.fasterxml.jackson.module.kotlin.treeToValue
import no.nav.tms.kafka.application.JsonMessage
import no.nav.tms.kafka.application.Subscriber
import no.nav.tms.kafka.application.Subscription
import no.nav.tms.statistikk.defaultDeserializer
import no.nav.tms.statistikk.varsel.BeredskapMetadata.Companion.beredskapMetadata

class VarselAktivertSubscriber(
    private val repository: VarselRepository
) : Subscriber() {

    private val objectMapper = defaultDeserializer()

    override fun subscribe() = Subscription.forEvent("opprettet")
        .withFields(
            "varselId",
            "ident",
            "type",
            "opprettet",
            "produsent",
            "innhold",
            "sensitivitet"
        )
        .withOptionalFields(
            "aktivFremTil",
            "eksternVarslingBestilling",
            "metadata"
        )

    override suspend fun receive(jsonMessage: JsonMessage) {
        val aktivertVarsel: AktivertVarsel = objectMapper.treeToValue(jsonMessage.json)
        repository.insertVarsel(aktivertVarsel)

        jsonMessage.beredskapMetadata()?.let {
            repository.insertBeredskapReference(it, jsonMessage["varselId"].asText())
        }
    }
}

class BeredskapMetadata private constructor(
    val tittel: String?,
    val ref: String?
) {
    val isEmpty = tittel == null && ref == null

    companion object {
        fun JsonMessage.beredskapMetadata() = BeredskapMetadata(
            getOrNull("metadata")?.findValue("beredskap_tittel")?.asText(),
            getOrNull("metadata")?.findValue("beredskap_ref")?.asText()
        ).let {
            if (it.isEmpty)
                null
            else it
        }
    }
}
