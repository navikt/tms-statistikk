package no.nav.tms.statistikk.eksternVarsling

import LocalPostgresDatabase
import assert
import cleanTables
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import no.nav.helse.rapids_rivers.testsupport.TestRapid
import no.nav.tms.statistikk.database.DateTimeHelper
import no.nav.tms.statistikk.database.DateTimeHelper.nowAtUtcZ
import no.nav.tms.statistikk.varsel.VarselAktivertSink
import no.nav.tms.statistikk.varsel.VarselRepository
import no.nav.tms.statistikk.varsel.VarselTestData
import no.nav.tms.statistikk.varsel.getVarsel
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.util.*

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class EksternVarslingSinkTest {

    private val db = LocalPostgresDatabase.cleanDb()
    private val testRapid = TestRapid()
    private val varselRepository = VarselRepository(db)

    init {
        EksternVarslingSink(testRapid, EksternVarslingRepository(db))
        VarselAktivertSink(testRapid, varselRepository)
    }


    @AfterEach
    fun cleanup() {
        db.cleanTables("innlogging_etter_eksternt_varsel")
    }

    @Test
    fun `Plukker opp ekstern varsling sendt`() {
        testRapid.sendTestMessage(SMS.testMessage("123"))
        testRapid.sendTestMessage(EPOST.testMessage("124"))

        db.getEksternVarsling("123").first() shouldNotBe null
        db.getEksternVarsling("124").first() shouldNotBe null
    }

    @Test
    fun `Oppdaterer varslingskanaler`() {
        testRapid.sendTestMessage(SMS.testMessage("123"))
        testRapid.sendTestMessage(EPOST.testMessage("123"))
        db.getEksternVarsling("123").assert {
            size shouldNotBe 0
            first()["sms"] shouldBe true
            first()["epost"] shouldBe true
        }

        testRapid.sendTestMessage(EPOST.testMessage("124"))
        testRapid.sendTestMessage(SMS.testMessage("124"))

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
        val previous = DateTimeHelper.nowAtUtc().minusDays(11)

        db.insertEksterntTestVarsel(testEvent, testIdent, previous, EPOST)

        testRapid.sendTestMessage(SMS.testMessage(testEvent, testIdent))
        testRapid.sendTestMessage(EPOST.testMessage(testEvent, testIdent))

        db.getEksternVarsling(eventId = testEvent).assert {
            size shouldBe 2
        }
    }

    @Test
    fun `oppdaterer status om ekstern varsling`() {
        val eventId1 = UUID.randomUUID().toString()
        val eventId2 = UUID.randomUUID().toString()
        val eventId3 = UUID.randomUUID().toString()

        testRapid.sendTestMessage(VarselTestData.varselAktivertMessage(varselId = eventId1, eksternVarsling = true))
        testRapid.sendTestMessage(SMS.testMessage(eventId1))

        testRapid.sendTestMessage(VarselTestData.varselAktivertMessage(varselId = eventId2, eksternVarsling = true))
        testRapid.sendTestMessage(EPOST.testMessage(eventId2))

        testRapid.sendTestMessage(VarselTestData.varselAktivertMessage(varselId = eventId3, eksternVarsling = true))
        testRapid.sendTestMessage(EPOST.testMessage(eventId3))
        testRapid.sendTestMessage(SMS.testMessage(eventId3))

        val varsel1 = db.getVarsel(eventId1)!!
        varsel1.eksternVarslingSendtSms shouldBe true
        varsel1.eksternVarslingSendtEpost shouldBe false

        val varsel2 = db.getVarsel(eventId2)!!
        varsel2.eksternVarslingSendtSms shouldBe false
        varsel2.eksternVarslingSendtEpost shouldBe true

        val varsel3 = db.getVarsel(eventId3)!!
        varsel3.eksternVarslingSendtSms shouldBe true
        varsel3.eksternVarslingSendtEpost shouldBe true
    }
}


private fun String.testMessage(eventId: String, ident: String = eksternVarslingTestIdent) = """{
      "@event_name": "eksternStatusOppdatert",
      "@source": "varsel-authority",
      "ident": "$ident",
      "status": "sendt",
      "varselId": "$eventId",
      "varselType": "oppgave",
      "namespace": "pto",
      "appnavn": "veilarbaktivitet",
      "kanal": "${this}",
      "tidspunkt": "${nowAtUtcZ()}"
    }"""
