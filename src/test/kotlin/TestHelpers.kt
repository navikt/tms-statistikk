
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
fun utkastDeletedMelding(utkastId: String = "123") = """
    {
    "@event_name":"deleted",
    "utkastId":"$utkastId"
    }
""".trimIndent()
