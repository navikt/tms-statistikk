package no.nav.tms.statistikk

import kotliquery.queryOf
import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.MessageContext
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.helse.rapids_rivers.River
import no.nav.tms.statistikk.database.Database
import java.time.LocalDate
import java.time.ZoneId

internal class UtkastSink(
    rapidsConnection: RapidsConnection,
    val utkastPersistance: UtkastPersistance
) : River.PacketListener {
    init {
        River(rapidsConnection).apply {
            validate { it.demandAny("@event_name", listOf("created", "deleted")) }
            validate { it.requireKey("utkastId") }
            validate { it.interestedIn("ident", "utkastId", "tittel_i18n") }
        }.register(this)

    }

    override fun onPacket(packet: JsonMessage, context: MessageContext) {
        utkastPersistance.put(
            utkastId = packet["utkastId"].asText(),
            ident = packet["ident"].asText(),
            event = packet["@event_name"].asText(),
            antallSpråk = packet.antallSpråk()
        )
    }

}

private fun JsonMessage.antallSpråk(): Int =
    this["tittel_i18n"].toList().size.let {
        if(it == 0)
            1
        else it
    }


internal class UtkastPersistance(val database: Database) {
    fun put(utkastId: String, ident: String, event: String, antallSpråk: Int) {
        database.update {
  /*          ident TEXT NOT NULL,
            utkast_id TEXT NOT NULL UNIQUE,
            dato DATE NOT NULL,
            event TEXT NOT NULL,
            antall_språk INT NOT NULL*/

            //language=PostgreSQL
            queryOf(
                """INSERT INTO utkast VALUES(:ident,:utkastId,:dato,:event,:antallSprak) 
                    |ON CONFLICT(utkast_id)
                    |DO UPDATE SET event=:event, dato=:dato, antall_språk=:antallSprak
                    |""".trimMargin(),
                mapOf(
                    "ident" to ident,
                    "utkastId" to utkastId,
                    "dato" to LocalDate.now(ZoneId.of("UTC")),
                    "event" to event,
                    "antallSprak" to antallSpråk
                )
            )
        }
    }
}