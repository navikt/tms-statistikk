package no.nav.tms.statistikk.eksternVarsling

import LocalPostgresDatabase
import assert
import cleanTables
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import kotliquery.queryOf
import no.nav.helse.rapids_rivers.testsupport.TestRapid
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.time.LocalDateTime

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class EksternVarslingSinkTest {

    private val db = LocalPostgresDatabase.cleanDb()
    private val testRapid = TestRapid()

    @BeforeAll
    fun startSink() {
        EksternVarslingSink(testRapid, EksternVarslingRepository(db))
    }

    @AfterEach
    fun cleanup() {
        db.cleanTables("innlogging_etter_eksternt_varsel")
    }

    @Test
    fun `Plukker opp ekstern varsling sendt`() {
        testRapid.sendTestMessage(Kanal.SMS.testMessage("123"))
        testRapid.sendTestMessage(Kanal.EPOST.testMessage("124"))

        db.getEksternVarsling("123") shouldNotBe null
        db.getEksternVarsling("124") shouldNotBe null
    }

    @Test
    fun `Oppdaterer varslingskanaler`() {
        testRapid.sendTestMessage(Kanal.SMS.testMessage("123"))
        testRapid.sendTestMessage(Kanal.EPOST.testMessage("123"))
        db.getEksternVarsling("123").assert {
            size shouldNotBe null
            first()["sms"] shouldBe true
            first()["epost"] shouldBe true
        }

        testRapid.sendTestMessage(Kanal.EPOST.testMessage("124"))
        testRapid.sendTestMessage(Kanal.SMS.testMessage("124"))
        db.getEksternVarsling("123").assert {
            size shouldNotBe null
            first()["sms"] shouldBe true
            first()["epost"] shouldBe true
        }

    }

    @Test
    fun `Plukker opp revarsling sendt`() {
        val testEvent = "23456789"
        val testIdent = "987654"
        val now = LocalDateTime.now()

        db.update {
            queryOf(
                "insert into innlogging_etter_eksternt_varsel values(:eventId,:ident,:nowTime)",
                mapOf("eventId" to testEvent, "ident" to testIdent, "nowTime" to now.minusDays(7))
            )
        }

        testRapid.sendTestMessage(Kanal.SMS.testMessage("123"))
        db.getEksternVarsling(eventId = testEvent).assert {
            size shouldBe 2
            first()["sendtDato"] shouldBe now.minusDays(7)
            last()["sendtDato"] shouldBe now
            last()["sms"] shouldBe true
            last()["epost"] shouldBe false
        }
    }
}


private fun Kanal.testMessage(eventId: String) = """{
      "@event_name": "eksternStatusOppdatert",
      "status": "sendt",
      "eventId": "$eventId",
      "varselType": "oppgave",
      "namespace": "pto",
      "appnavn": "veilarbaktivitet",
      "kanal": "${this.name}"
    }"""

