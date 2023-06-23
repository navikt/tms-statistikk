package no.nav.tms.statistikk.varsel

import com.fasterxml.jackson.databind.JsonNode
import mu.KotlinLogging
import no.nav.helse.rapids_rivers.*

class VarselAktivertSink(
    rapidsConnection: RapidsConnection,
    private val repository: VarselRepository
): River.PacketListener {

    private val log = KotlinLogging.logger {}

    init {
        River(rapidsConnection).apply {
            validate {
                it.demandValue("@event_name", "aktivert")
                it.rejectValue("@source", "varsel-authority")
                it.requireKey(
                    "eventId",
                    "fodselsnummer",
                    "varselType",
                    "forstBehandlet",
                    "eksternVarsling",
                    "namespace",
                    "appnavn",
                    "tekst",
                    "link",
                    "sikkerhetsnivaa",
                    "forstBehandlet",
                    "eksternVarsling"
                )
                it.interestedIn("synligFremTil")
            }
        }.register(this)
    }

    override fun onPacket(packet: JsonMessage, context: MessageContext) {
        repository.insertVarsel(deserializeVarselAktivert(packet))
    }

    override fun onError(problems: MessageProblems, context: MessageContext) {
        log.info(problems.toString())
    }

    fun deserializeVarselAktivert(json: JsonMessage) = Varsel(
        eventId = json["eventId"].textValue(),
        ident = json["fodselsnummer"].textValue(),
        type = json["varselType"].textValue(),
        namespace = json["namespace"].textValue(),
        appnavn = json["appnavn"].textValue(),
        tekstlengde = json["tekst"].textValue().length,
        lenke = json["link"].isNotNullOrEmptyString(),
        sikkerhetsnivaa = json["sikkerhetsnivaa"].intValue(),
        aktiv = true,
        forstBehandlet = json["forstBehandlet"].asLocalDateTime(),
        frist = json["synligFremTil"].isNotNullOrEmptyString(),
        inaktivertTidspunkt = null,
        inaktivertKilde = null,
        eksternVarslingBestilt = json["eksternVarsling"].booleanValue(),
        eksternVarslingSendtSms = false,
        eksternVarslingSendtEpost = false
    )

    private fun JsonNode.isNotNullOrEmptyString(): Boolean {
        return when {
            isNull -> false
            !isTextual -> false
            else -> textValue().isNotBlank()
        }
    }
}
