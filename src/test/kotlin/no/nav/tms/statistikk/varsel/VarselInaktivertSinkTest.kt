package no.nav.tms.statistikk.varsel

import io.kotest.matchers.shouldBe
import kotliquery.queryOf
import no.nav.helse.rapids_rivers.testsupport.TestRapid
import no.nav.tms.statistikk.LocalDateTimeHelper
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.*

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class VarselInaktivertSinkTest {
    private val database = LocalPostgresDatabase.cleanDb()
    private val varselRepository = VarselRepository(database)
    private val testRapid = TestRapid()

    @BeforeAll
    fun setupSinks() {
        VarselAktivertSink(testRapid, varselRepository)
        VarselInaktivertSink(testRapid, varselRepository)
    }

    @AfterEach
    fun cleanDb() {
        database.update { queryOf("delete from varsel") }
    }

    @Test
    fun `registrerer at varsel har blitt inaktivert`() {
        val eventId = UUID.randomUUID().toString()
        val kilde = "bruker"
        val inaktivertTidspunkt = LocalDateTimeHelper.nowAtUtc().minusHours(1)

        testRapid.sendTestMessage(VarselTestData.varselAktivertMessage(eventId = eventId))
        testRapid.sendTestMessage(VarselTestData.varselInaktivertMessage(eventId = eventId, kilde = kilde, tidspunkt = inaktivertTidspunkt))

        val varsel = database.getVarsel(eventId)!!

        varsel.aktiv shouldBe false
        varsel.inaktivertTidspunkt shouldBe inaktivertTidspunkt
        varsel.inaktivertKilde shouldBe kilde
    }
}
