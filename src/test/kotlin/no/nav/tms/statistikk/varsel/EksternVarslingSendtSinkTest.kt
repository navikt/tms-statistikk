package no.nav.tms.statistikk.varsel

import io.kotest.matchers.shouldBe
import kotliquery.queryOf
import no.nav.helse.rapids_rivers.testsupport.TestRapid
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import java.util.*

internal class EksternVarslingSendtSinkTest {
    private val database = LocalPostgresDatabase.cleanDb()
    private val varselRepository = VarselRepository(database)
    private val testRapid = TestRapid()

    @BeforeAll
    fun setupSinks() {
        VarselAktivertSink(testRapid, varselRepository)
        EksternVarslingSendtSink(testRapid, varselRepository)
    }

    @AfterEach
    fun cleanDb() {
        database.update { queryOf("delete from varsel") }
    }

    @Test
    fun `oppdaterer status om ekstern varsling`() {
        val eventId1 = UUID.randomUUID().toString()
        val eventId2 = UUID.randomUUID().toString()
        val eventId3 = UUID.randomUUID().toString()

        testRapid.sendTestMessage(VarselTestData.varselAktivertMessage(eventId = eventId1, eksternVarsling = true))
        testRapid.sendTestMessage(VarselTestData.eksternVarslingSendt(eventId = eventId1, kanal = "SMS"))
        testRapid.sendTestMessage(VarselTestData.varselAktivertMessage(eventId = eventId2, eksternVarsling = true))
        testRapid.sendTestMessage(VarselTestData.eksternVarslingSendt(eventId = eventId2, kanal = "EPOST"))
        testRapid.sendTestMessage(VarselTestData.varselAktivertMessage(eventId = eventId3, eksternVarsling = true))
        testRapid.sendTestMessage(VarselTestData.eksternVarslingSendt(eventId = eventId3, kanal = "SMS"))
        testRapid.sendTestMessage(VarselTestData.eksternVarslingSendt(eventId = eventId3, kanal = "EPOST"))

        val varsel1 = database.getVarsel(eventId1)!!
        varsel1.eksternVarslingSendtSms shouldBe true
        varsel1.eksternVarslingSendtEpost shouldBe false

        val varsel2 = database.getVarsel(eventId2)!!
        varsel2.eksternVarslingSendtSms shouldBe false
        varsel2.eksternVarslingSendtEpost shouldBe true

        val varsel3 = database.getVarsel(eventId3)!!
        varsel3.eksternVarslingSendtSms shouldBe true
        varsel3.eksternVarslingSendtEpost shouldBe true
    }
}
