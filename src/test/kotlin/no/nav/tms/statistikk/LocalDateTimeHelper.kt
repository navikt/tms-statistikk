package no.nav.tms.statistikk

import java.time.LocalDateTime
import java.time.ZoneId
import java.time.temporal.ChronoUnit


object LocalDateTimeHelper {
    fun nowAtUtc() = LocalDateTime.now(ZoneId.of("UTC")).truncatedTo(ChronoUnit.MILLIS)
}
