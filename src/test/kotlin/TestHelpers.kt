
import org.intellij.lang.annotations.Language

@Language("JSON")
fun utkastCreatedMelding(utkastId: String = "123", ident: String = "887766") = """
    {
    "@event_name":"created",
    "utkastId":"$utkastId",
    "ident": "$ident"
    }
""".trimIndent()

@Language("JSON")
fun utkastUpdatedMelding(utkastId: String = "123", ident: String = "887766") = """
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
fun utkastDeletedMelding(utkastId: String = "123") = """
    {
    "@event_name":"deleted",
    "utkastId":"$utkastId"
    }
""".trimIndent()
