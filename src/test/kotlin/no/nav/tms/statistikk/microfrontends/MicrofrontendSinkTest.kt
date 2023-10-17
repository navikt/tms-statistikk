package no.nav.tms.statistikk.microfrontends

import LocalPostgresDatabase
import assert
import io.kotest.matchers.shouldBe
import kotliquery.queryOf
import no.nav.helse.rapids_rivers.testsupport.TestRapid
import no.nav.tms.statistikk.database.LocalDateTimeHelper
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit.MINUTES

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class MicrofrontendSinkTest {
    private val database = LocalPostgresDatabase.cleanDb()
    private val testRapid = TestRapid()
    private val testFnr = "12345678910"

    @BeforeAll
    fun setup() {
        MicrofrontendSink(
            testRapid,
            MicrofrontendRepository(database)
        )
    }

    @Test
    fun `plukker opp enable`() {

        //NB; kan ende opp med dobbelresultat?
        enableMelding(ident = testFnr, "mk1").send()
        enableMelding(ident = testFnr, "mk1").send()
        enableMelding(ident = testFnr, "mk2").send()
        enableMelding(ident = testFnr, "mk4").send()

        val microfrontends = database.getAll()
        microfrontends.find { it.microfrontendtId == "mk2" }.assert {
            require(this != null)
            ident shouldBe testFnr
            time.truncatedTo(MINUTES) shouldBe LocalDateTimeHelper.nowAtUtcMinutes()
            action shouldBe "enable"
        }
    }

    private fun String.send() {
        testRapid.sendTestMessage(this)
    }
}

private fun LocalDateTimeHelper.nowAtUtcMinutes() = nowAtUtc().truncatedTo(MINUTES)

private fun LocalPostgresDatabase.getAll() =
    list {
        queryOf("select * from microfrontends").map { row ->
            MicrofrontendResult(
                ident = row.string("ident"),
                time = row.localDateTime("time"),
                action = row.string("action"),
                microfrontendtId = row.string("microfrontend_id")
            )
        }.asList
    }

private fun enableMelding(ident: String, microfrontendtId: String) =
    """{
        "@action":"enable", 
       "ident": "$ident", 
       "microfrontend_id": "$microfrontendtId",
       "@initiated_by": "testteam", 
       "sensitivitet" : "high"
       }
       """.trimMargin()

private fun disableMelding(ident: String, microfrontendtId: String) =
    """{
        @action":"enable", 
       "ident": "$ident", 
       "microfrontend_id": "$microfrontendtId",
       "@initiated_by": "testteam", 
       "sensitivitet" : "high"
       }
       """.trimMargin()

private data class MicrofrontendResult(
    val ident: String,
    val time: LocalDateTime,
    val action: String,
    val microfrontendtId: String
)