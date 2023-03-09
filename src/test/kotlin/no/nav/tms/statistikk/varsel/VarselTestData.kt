package no.nav.tms.statistikk.varsel

import no.nav.tms.statistikk.varsel.VarselTestData.VarselType.beskjed
import java.time.LocalDateTime
import java.time.LocalDateTime.now
import java.util.*

object VarselTestData {
    enum class VarselType { beskjed, oppgave, innboks }

    fun varselAktivertMessage(
        ident: String = "123",
        type: VarselType = beskjed,
        eventId: String = UUID.randomUUID().toString(),
        eksternVarsling: Boolean = false,
        forstBehandlet: LocalDateTime = now(),
        namespace: String = "namespace",
        appnavn: String = "appnavn",
        tekst: String = "tekst",
        link: String = "https://link",
        sikkerhetsnivaa: Int = 3,
        sistOppdatert: LocalDateTime = now(),
        synligFremTil: LocalDateTime? = now().plusWeeks(2),
    )
    = """
        {
            "@event_name": "aktivert",
            "fodselsnummer": "$ident",
            "varselType": "${type.name}",
            "eksternVarsling": $eksternVarsling,
            "forstBehandlet": "$forstBehandlet",
            "eventId": "$eventId",
            "namespace": "$namespace",
            "appnavn": "$appnavn",
            "tekst": "$tekst",
            "link": "$link",
            "sikkerhetsnivaa": $sikkerhetsnivaa,
            ${if (synligFremTil != null) "\"synligFremTil\":\"$synligFremTil\"," else ""}
            "sistOppdatert": "$sistOppdatert"
        }
    """

    fun varselInaktivertMessage(
        type: VarselType = beskjed,
        eventId: String = UUID.randomUUID().toString(),
        namespace: String = "namespace",
        appnavn: String = "appnavn",
        kilde: String = "produsent",
        tidspunkt: LocalDateTime = now()
    )
    = """
        {
            "@event_name": "inaktivert",
            "eventId": "$eventId",
            "varselType": "${type.name}",
            "namespace": "$namespace",
            "appnavn": "$appnavn",
            "kilde": "$kilde",
            "tidspunkt": "$tidspunkt"
        }
    """

    fun eksternVarslingSendt(
        varselType: VarselType = beskjed,
        eventId: String = UUID.randomUUID().toString(),
        kanal: String = "SMS",
        namespace: String = "namespace",
        appnavn: String = "appnavn",
        tidspunkt: LocalDateTime = now()
    )
    = """
       {
            "@event_name": "eksternStatusOppdatert",
            "status": "sendt",
            "eventId": "$eventId",
            "kanal": "$kanal",
            "varselType": "${varselType.name}",
            "namespace": "$namespace",
            "appnavn": "$appnavn",
            "tidspunkt": "$tidspunkt"
       }
    """
}
