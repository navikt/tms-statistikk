package no.nav.tms.statistikk.varsel

import com.fasterxml.jackson.module.kotlin.readValue
import io.github.oshai.kotlinlogging.KotlinLogging
import no.nav.helse.rapids_rivers.*
import no.nav.tms.statistikk.defaultDeserializer
import no.nav.tms.statistikk.varsel.BeredskapMetadata.Companion.beredskapMetadata

class VarselAktivertSink(
    rapidsConnection: RapidsConnection,
    private val repository: VarselRepository
) : River.PacketListener {

    private val log = KotlinLogging.logger {}

    private val objectMapper = defaultDeserializer()

    init {
        River(rapidsConnection).apply {
            validate {
                it.demandValue("@event_name", "opprettet")
                it.requireKey(
                    "varselId",
                    "ident",
                    "type",
                    "opprettet",
                    "produsent",
                    "innhold",
                    "sensitivitet"
                )
                it.interestedIn("aktivFremTil", "eksternVarslingBestilling", "metadata")
            }
        }.register(this)
    }

    override fun onPacket(packet: JsonMessage, context: MessageContext) {
        val aktivertVarsel: AktivertVarsel = objectMapper.readValue(packet.toJson())
        repository.insertVarsel(aktivertVarsel)
        packet.beredskapMetadata()?.let {
            repository.insertBeredskapReference(it, packet["varselId"].asText())
        }
    }

    override fun onError(problems: MessageProblems, context: MessageContext) {
        log.info { problems.toString() }
    }
}

class BeredskapMetadata private constructor(
    val tittel: String?,
    val ref: String?
) {
    val isEmpty = tittel == null && ref == null

    companion object {
        fun JsonMessage.beredskapMetadata() = BeredskapMetadata(
            this["metadata"].findValue("beredskap_tittel")?.asText(),
            this["metadata"].findValue("beredskap_ref")?.asText()
        ).let {
            if (it.isEmpty)
                null
            else it
        }
    }
}
