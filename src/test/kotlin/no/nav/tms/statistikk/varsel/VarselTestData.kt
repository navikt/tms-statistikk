package no.nav.tms.statistikk.varsel

import no.nav.tms.statistikk.database.DateTimeHelper.nowAtUtcZ
import no.nav.tms.statistikk.varsel.VarselTestData.VarselType.beskjed
import java.time.ZonedDateTime
import java.util.*

object VarselTestData {
    enum class VarselType { beskjed, oppgave, innboks }

    fun varselAktivertMessage(
        ident: String = "123",
        type: VarselType = beskjed,
        varselId: String = UUID.randomUUID().toString(),
        eksternVarsling: Boolean = false,
        opprettet: ZonedDateTime = nowAtUtcZ(),
        namespace: String = "namespace",
        appnavn: String = "appnavn",
        tekst: String = "tekst",
        link: String = "https://link",
        sensitivitet: Sensitivitet = Sensitivitet.Substantial,
        sistOppdatert: ZonedDateTime = nowAtUtcZ(),
        aktivFremTil: ZonedDateTime? = nowAtUtcZ().plusWeeks(2),
    )
    = """
        {
            "@event_name": "aktivert",
            "ident": "$ident",
            "type": "${type.name}",
            ${if (eksternVarsling) {
                    """"eksternVarslingBestilling": { "prefererteKanaler": [] },"""
                } else {
                    ""
                }
            }
            "opprettet": "$opprettet",
            "varselId": "$varselId",
            "produsent": {
                "namespace": "$namespace",
                "appnavn": "$appnavn"
            },
            "innhold": {
                "tekst": "$tekst",
                "link": "$link"
            },
            "sensitivitet": "${sensitivitet.lowercaseName}",
            ${if (aktivFremTil != null) "\"aktivFremTil\":\"$aktivFremTil\"," else ""}
            "sistOppdatert": "$sistOppdatert"
        }
    """

    fun varselInaktivertMessage(
        type: VarselType = beskjed,
        varselId: String = UUID.randomUUID().toString(),
        namespace: String = "namespace",
        appnavn: String = "appnavn",
        kilde: String = "produsent",
        tidspunkt: ZonedDateTime = nowAtUtcZ()
    )
    = """
        {
            "@event_name": "inaktivert",
            "varselId": "$varselId",
            "varselType": "${type.name}",
            "namespace": "$namespace",
            "appnavn": "$appnavn",
            "kilde": "$kilde",
            "tidspunkt": "$tidspunkt"
        }
    """

    fun eksternVarslingSendt(
        varselType: VarselType = beskjed,
        varselId: String = UUID.randomUUID().toString(),
        kanal: String = "SMS",
        namespace: String = "namespace",
        appnavn: String = "appnavn",
        tidspunkt: ZonedDateTime = nowAtUtcZ()
    )
    = """
       {
            "@event_name": "eksternStatusOppdatert",
            "status": "sendt",
            "varselId": "$varselId",
            "kanal": "$kanal",
            "varselType": "${varselType.name}",
            "namespace": "$namespace",
            "appnavn": "$appnavn",
            "tidspunkt": "$tidspunkt"
       }
    """
}
