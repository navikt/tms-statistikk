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
        cluster: String = "cluster",
        namespace: String = "namespace",
        appnavn: String = "appnavn",
        tekst: String = "tekst",
        link: String = "https://link",
        sensitivitet: Sensitivitet = Sensitivitet.Substantial,
        sistOppdatert: ZonedDateTime = nowAtUtcZ(),
        aktivFremTil: ZonedDateTime? = nowAtUtcZ().plusWeeks(2),
    ) = """
        {
            "@event_name": "opprettet",
            "ident": "$ident",
            "type": "${type.name}",
            ${
        if (eksternVarsling) {
            """"eksternVarslingBestilling": { "prefererteKanaler": [] },"""
        } else {
            ""
        }
    }
            "opprettet": "$opprettet",
            "varselId": "$varselId",
            "produsent": {
                "cluster": "$cluster",
                "namespace": "$namespace",
                "appnavn": "$appnavn"
            },
            "innhold": {
                "tekster": [ 
                    {
                        "tekst": "$tekst",
                        "spraakkode": "nb",
                        "default": true
                    },
                    {
                        "tekst": "Annen tekst",
                        "spraakkode": "en",
                        "default": false
                    }
                 ],
                "link": "$link"
            },
            "sensitivitet": "${sensitivitet.lowercaseName}",
            ${if (aktivFremTil != null) "\"aktivFremTil\":\"$aktivFremTil\"," else ""}
            "sistOppdatert": "$sistOppdatert"
        }
    """

    internal fun String.addBeredskapMetadata(beredskapTittel: String, beredskapRef:String = "123") =
        this.substring(0, lastIndexOf("}")).let {
            """$it,                    
            "metadata": {
              "beredskap_tittel": "$beredskapTittel",
              "beredskap_ref": "$beredskapRef"
              }    
            }
                """.trimMargin()
        }


    fun varselInaktivertMessage(
        type: VarselType = beskjed,
        varselId: String = UUID.randomUUID().toString(),
        cluster: String = "cluster",
        namespace: String = "namespace",
        appnavn: String = "appnavn",
        kilde: String = "produsent",
        tidspunkt: ZonedDateTime = nowAtUtcZ()
    ) = """
        {
            "@event_name": "inaktivert",
            "varselId": "$varselId",
            "varseltype": "${type.name}",
            "produsent": {
                "cluster": "$cluster",
                "namespace": "$namespace",
                "appnavn": "$appnavn"                        
            },
            "kilde": "$kilde",
            "tidspunkt": "$tidspunkt"
        }
    """

    fun eksternVarslingSendt(
        varseltype: VarselType = beskjed,
        varselId: String = UUID.randomUUID().toString(),
        kanal: String = "SMS",
        cluster: String = "cluster",
        namespace: String = "namespace",
        appnavn: String = "appnavn",
        tidspunkt: ZonedDateTime = nowAtUtcZ()
    ) = """
       {
            "@event_name": "eksternStatusOppdatert",
            "status": "sendt",
            "varselId": "$varselId",
            "kanal": "$kanal",
            "varseltype": "${varseltype.name}",
            "produsent": {
                "cluster": "$cluster",
                "namespace": "$namespace",
                "appnavn": "$appnavn"                        
            },
            "tidspunkt": "$tidspunkt"
       }
    """
}
