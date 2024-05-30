package no.nav.tms.statistikk.microfrontends

import LocalPostgresDatabase
import assert
import io.kotest.assertions.withClue
import io.kotest.matchers.shouldBe
import kotliquery.queryOf
import no.nav.tms.kafka.application.MessageBroadcaster
import no.nav.tms.statistikk.database.DateTimeHelper
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit.MINUTES

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class MicrofrontendSubscriberTest {
    private val database = LocalPostgresDatabase.cleanDb()
    private val testFnr = "12345678910"

    private val broadcaster = MessageBroadcaster(
        listOf(MicrofrontendSubscriber(MicrofrontendRepository(database))),
        eventNameFields = listOf("@action")
    )

    @BeforeAll
    fun setup() {

    }

    @AfterEach
    fun cleanup() {
        database.update { queryOf("delete from microfrontends") }
    }

    @Test
    fun `plukker opp enable`() {

        enableMelding(ident = testFnr, "mk1").send()
        enableMelding(ident = testFnr, "mk1").send()
        enableMelding(ident = testFnr, "mk2").send()
        enableMelding(ident = testFnr, "mk4").send()

        val microfrontends = database.getAll()
        microfrontends.filter { it.microfrontendId == "mk1" }.size shouldBe 1
        microfrontends.find { it.microfrontendId == "mk2" }.assert {
            require(this != null)
            ident shouldBe testFnr
            time.truncatedTo(MINUTES) shouldBe DateTimeHelper.nowAtUtcMinutes()
            action shouldBe "enable"
            initiatedBy shouldBe "testteam"
        }
    }

    @Test
    fun `plukker opp disable`() {

        enableMelding(ident = testFnr, "mk1").send()
        disableMelding(ident = testFnr, "mk1").send()
        disableMelding(ident = testFnr, "mk1").send()

        disableMelding(ident = testFnr, "mk2").send()
        enableMelding(ident = testFnr, "mk2").send()

        enableMelding(ident = testFnr, "mk4").send()
        eldredisableMelding(ident = testFnr, "mk4").send()


        val microfrontends = database.getAll()
        microfrontends.filter { it.microfrontendId == "mk1" }.assert {
            withClue("Feil antall for mk1 (enable -> disable ->disable)") { size shouldBe 2 }

            first().action shouldBe "enable"
            last().action shouldBe "disable"
            withClue(microfrontends.joinToString {
                "Feil i initiatedby felt. faktiske verdier: ${it.initiatedBy}"
            }) { all { it.initiatedBy == "testteam" } shouldBe true }
        }

        microfrontends.filter { it.microfrontendId == "mk2" }.assert {
            withClue("Feil antall for mk2 (disable -> enable)") { size shouldBe 1 }
            first().action shouldBe "enable"
        }

        microfrontends.filter { it.microfrontendId == "mk4" }.assert {
            withClue("Feil antall for mk4 (enable -> eldre disable)") { size shouldBe 2 }
            first().action shouldBe "enable"
            last().action shouldBe "disable"
            withClue(microfrontends.joinToString {
                "Feil i initiatedby felt for gamle meldinger. faktiske verdier: ${it.initiatedBy}"
            }) { map { it.initiatedBy } shouldBe listOf("testteam", null) }
        }
    }

    private fun String.send() {
        broadcaster.broadcastJson(this)
    }
}
private fun LocalPostgresDatabase.getAll() =
    list {
        queryOf("select * from microfrontends").map { row ->
            MicrofrontendResult(
                ident = row.string("ident"),
                time = row.localDateTime("initiated_time"),
                action = row.string("action"),
                microfrontendId = row.string("microfrontend_id"),
                initiatedBy = row.stringOrNull("initiated_by")
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
        "@action":"disable", 
       "ident": "$ident", 
       "microfrontend_id": "$microfrontendtId",
       "@initiated_by": "testteam" 
       }
       """.trimMargin()

private fun eldredisableMelding(ident: String, microfrontendtId: String) =
    """{
        "@action":"disable", 
       "ident": "$ident", 
       "microfrontend_id": "$microfrontendtId"
       }
       """.trimMargin()

private data class MicrofrontendResult(
    val ident: String,
    val time: LocalDateTime,
    val action: String,
    val microfrontendId: String,
    val initiatedBy: String?
)
