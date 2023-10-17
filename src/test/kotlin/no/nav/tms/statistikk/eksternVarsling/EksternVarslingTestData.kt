package no.nav.tms.statistikk.eksternVarsling

import kotliquery.queryOf
import no.nav.tms.statistikk.database.Database
import no.nav.tms.statistikk.database.DateTimeHelper
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

const val eksternVarslingTestIdent = "987654"

val EPOST ="epost"
val SMS ="sms"

internal fun Database.getEksternVarsling(eventId: String) = list {
    queryOf(
        """select eventid,ident,sendttimestamp,epost,sms from innlogging_etter_eksternt_varsel
                    |where eventid=:eventId""".trimMargin(),
        mapOf("eventId" to eventId)
    ).map {
        mapOf(
            "eventId" to it.string("eventid"),
            "ident" to it.string("ident"),
            "sendt" to it.localDateTime("sendttimestamp"),
            "epost" to it.boolean("epost"),
            "sms" to it.boolean("sms")
        )
    }.asList
}

internal fun Database.insertEksterntTestVarsel(
    eventId: String,
    ident: String,
    sentTime: LocalDateTime = DateTimeHelper.nowAtUtc(),
    kanal: String
) =
    update {
        queryOf(
            "insert into innlogging_etter_eksternt_varsel(eventid,dato,ident,sendttimestamp,epost,sms) values(:eventId,:nowTime::date,:ident,:nowTime,:epost,:sms)",
            mapOf(
                "eventId" to eventId,
                "ident" to ident,
                "nowTime" to sentTime,
                "epost" to (kanal.erSms()),
                "sms" to (kanal.erEpost())
            )
        )
    }

internal fun Any?.toDateTimeMinutes(): LocalDateTime {
    require(this != null)
    require(this is LocalDateTime)
    return toMinutes()
}

internal fun LocalDateTime.toMinutes() = this.truncatedTo(ChronoUnit.MINUTES)
private fun String.erSms() = lowercase() == "sms"
private fun String.erEpost() = lowercase() == "epost"
