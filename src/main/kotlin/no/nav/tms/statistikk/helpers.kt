package no.nav.tms.statistikk

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonMapperBuilder
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.ZonedDateTime


fun JsonNode.asUtcDateTime(): LocalDateTime =
    asText().let { ZonedDateTime.parse(it) }
        .toUtcLocalDateTime()


fun JsonNode.asOptionalUtcDateTime(): LocalDateTime? =
    takeIf(JsonNode::isTextual)
        ?.asText()
        ?.takeIf(String::isNotEmpty)
        ?.let { ZonedDateTime.parse(it) }
        ?.toUtcLocalDateTime()

fun defaultDeserializer() = jacksonMapperBuilder()
    .addModule(JavaTimeModule())
    .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
    .build()

fun ZonedDateTime.toUtcLocalDateTime() = withZoneSameInstant(ZoneOffset.UTC).toLocalDateTime()
