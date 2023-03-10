package no.nav.tms.statistikk.eksternVarsling

import LocalPostgresDatabase
import assert
import cleanTables
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import no.nav.helse.rapids_rivers.testsupport.TestRapid
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.time.LocalDateTime

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class EksternVarslingSinkTest {

    private val db = LocalPostgresDatabase.cleanDb()
    private val testRapid = TestRapid()

    init {
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

        db.getEksternVarsling("123").first() shouldNotBe null
        db.getEksternVarsling("124").first() shouldNotBe null
    }

    @Test
    fun `Oppdaterer varslingskanaler`() {
        testRapid.sendTestMessage(Kanal.SMS.testMessage("123"))
        testRapid.sendTestMessage(Kanal.EPOST.testMessage("123"))
        db.getEksternVarsling("123").assert {
            size shouldNotBe 0
            first()["sms"] shouldBe true
            first()["epost"] shouldBe true
        }

        testRapid.sendTestMessage(Kanal.EPOST.testMessage("124"))
        testRapid.sendTestMessage(Kanal.SMS.testMessage("124"))

        db.getEksternVarsling("123").assert {
            size shouldBe 1
            first()["sms"] shouldBe true
            first()["epost"] shouldBe true
        }

    }

    @Test
    fun `Plukker opp revarsling sendt`() {
        val testEvent = "23456789"
        val testIdent = "987654"
        val previous = LocalDateTime.now().minusDays(11)

        db.insertEksterntTestVarsel(testEvent,testIdent,previous,Kanal.EPOST)

        testRapid.sendTestMessage(Kanal.SMS.testMessage(testEvent, testIdent))
        testRapid.sendTestMessage(Kanal.EPOST.testMessage(testEvent, testIdent))

        db.getEksternVarsling(eventId = testEvent).assert {
            size shouldBe 2
        }
    }
}


private fun Kanal.testMessage(eventId: String, ident: String = eksternVarslingTestIdent) = """{
      "@event_name": "eksternStatusOppdatert",
      "ident": "$ident",
      "status": "sendt",
      "eventId": "$eventId",
      "varselType": "oppgave",
      "namespace": "pto",
      "appnavn": "veilarbaktivitet",
      "kanal": "${this.name}"
    }"""

