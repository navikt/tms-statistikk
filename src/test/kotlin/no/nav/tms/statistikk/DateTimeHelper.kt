package no.nav.tms.statistikk

import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit


object DateTimeHelper {
    fun nowAtUtcZ() = ZonedDateTime.now(ZoneId.of("UTC")).truncatedTo(ChronoUnit.MILLIS)
    fun nowAtUtc() = LocalDateTime.now(ZoneId.of("UTC")).truncatedTo(ChronoUnit.MILLIS)
}
