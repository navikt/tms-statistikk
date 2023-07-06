package no.nav.tms.statistikk.varsel

import com.fasterxml.jackson.annotation.JsonValue
import java.time.LocalDateTime
import java.time.ZonedDateTime

data class AktivertVarsel(
    val type: String,
    val varselId: String,
    val ident: String,
    val sensitivitet: Sensitivitet,
    val innhold: Innhold,
    val produsent: Produsent,
    val eksternVarslingBestilling: EksternVarslingBestilling? = null,
    val opprettet: ZonedDateTime,
    val aktivFremTil: ZonedDateTime? = null
) {
    val tekstLengde get() = innhold.tekst.length
    val harLenke get() = innhold.link.isNullOrBlank().not()
    val frist get() = aktivFremTil != null
    val eksternVarslingBestilt get() = eksternVarslingBestilling != null
}

data class Innhold(
    val tekst: String,
    val link: String?
)

enum class Sensitivitet(val loginLevel: Int) {
    Substantial(3),
    High(4);

    val lowercaseName = name.lowercase()

    @JsonValue
    fun toJson() = lowercaseName

    companion object {
        fun parse(string: String): Sensitivitet {
            return Sensitivitet.values()
                .filter { it.lowercaseName == string.lowercase() }
                .firstOrNull() ?: throw IllegalArgumentException("Could not parse sensitivitet $string")
        }
    }
}

data class Produsent(
    val namespace: String,
    val appnavn: String
)

data class EksternVarslingBestilling(
    val prefererteKanaler: List<String>,
    val smsVarslingstekst: String? = null,
    val epostVarslingstekst: String? = null,
    val epostVarslingstittel: String? = null,
)

data class VarselInaktivert(
    val varselId: String,
    val tidspunkt: LocalDateTime?,
    val kilde: String
)
