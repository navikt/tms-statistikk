import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.intellij.lang.annotations.Language

fun enableMessage(microfrontendId: String, fnr: String) = """
    {
      "@action": "enable",
      "ident": "$fnr",
      "microfrontend_id": "$microfrontendId"
    }
    """.trimIndent()

fun disableMessage(microfrontendId: String, fnr: String) = """
    {
      "@action": "disable",
      "ident": "$fnr",
      "microfrontend_id": "$microfrontendId"
    }
    """.trimIndent()

@Language("JSON")
internal fun utkastCreatedMelding(utkastId: String = "123", ident: String = "887766") = """
    {
    "@event_name":"created",
    "utkastId":"$utkastId",
    "ident": "$ident"
    }
""".trimIndent()

@Language("JSON")
internal fun utkastUpdatedMelding(utkastId: String = "123", ident: String = "887766") = """
    {
      "@event_name": "created",
      "utkastId": "$utkastId",
      "ident": "$ident",
      "tittel_i18n": {
        "en": "En tittel",
        "nb": "Annen tittel"
      }
    }
""".trimIndent()

@Language("JSON")
internal fun utkastDeletedMelding(utkastId: String = "123") = """
    {
    "@event_name":"deleted",
    "utkastId":"$utkastId"
    }
""".trimIndent()