package no.nav.tms.statistikk.varsel

import com.fasterxml.jackson.module.kotlin.readValue
import io.github.oshai.kotlinlogging.KotlinLogging
import no.nav.helse.rapids_rivers.*
import no.nav.tms.statistikk.defaultDeserializer

class VarselAktivertSink(
    rapidsConnection: RapidsConnection,
    private val repository: VarselRepository
): River.PacketListener {

    private val log = KotlinLogging.logger {}

    private val objectMapper = defaultDeserializer()

    init {
        River(rapidsConnection).apply {
            validate {
                it.demandValue("@event_name", "aktivert")
                it.demandValue("@source", "varsel-authority")
                it.requireKey(
                    "varselId",
                    "ident",
                    "type",
                    "opprettet",
                    "produsent",
                    "innhold",
                    "sensitivitet"
                )
                it.interestedIn("aktivFremTil","eksternVarslingBestilling")
            }
        }.register(this)
    }

    override fun onPacket(packet: JsonMessage, context: MessageContext) {
        val aktivertVarsel: AktivertVarsel = objectMapper.readValue(packet.toJson())

        repository.insertVarsel(aktivertVarsel)
    }

    override fun onError(problems: MessageProblems, context: MessageContext) {
        log.info { problems.toString() }
    }
}
