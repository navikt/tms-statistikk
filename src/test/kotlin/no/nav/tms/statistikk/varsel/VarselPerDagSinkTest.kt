package no.nav.tms.statistikk.varsel

import io.kotest.matchers.shouldBe
import io.micrometer.prometheus.PrometheusConfig
import io.micrometer.prometheus.PrometheusMeterRegistry
import kotliquery.queryOf
import no.nav.helse.rapids_rivers.testsupport.TestRapid
import no.nav.tms.statistikk.LocalDateTimeHelper
import no.nav.tms.statistikk.varsel.VarselTestData.VarselType.*
import no.nav.tms.statistikk.varsel.VarselTestData.varselAktivertMessage
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.LocalDateTime

internal class VarselPerDagSinkTest {

    private val database = LocalPostgresDatabase.cleanDb()
    private val registry = PrometheusMeterRegistry(PrometheusConfig.DEFAULT)
    private val varselRepository = VarselRepository(database)
    private val testRapid = TestRapid()



    @BeforeAll
    fun setupSinks() {
        VarselPerDagSink(testRapid, varselRepository)
    }

    @AfterEach
    fun cleanDb() {
        database.update { queryOf("delete from varsler_per_dag") }
        registry.clear()
    }

    @Test
    fun `teller opp ulike typer varsler`() {
        val ident = "12345"

        testRapid.sendTestMessage(varselAktivertMessage(ident, beskjed))
        testRapid.sendTestMessage(varselAktivertMessage(ident, beskjed))
        testRapid.sendTestMessage(varselAktivertMessage(ident, beskjed))
        testRapid.sendTestMessage(varselAktivertMessage(ident, oppgave))
        testRapid.sendTestMessage(varselAktivertMessage(ident, oppgave))
        testRapid.sendTestMessage(varselAktivertMessage(ident, innboks))

        database.antallVarsler(ident) shouldBe 6
        database.antallVarsler(ident, beskjed) shouldBe 3
        database.antallVarsler(ident, oppgave) shouldBe 2
        database.antallVarsler(ident, innboks) shouldBe 1
    }

    @Test
    fun `teller ekstern varsling eller ikke`() {
        val ident = "12345"

        testRapid.sendTestMessage(varselAktivertMessage(ident, beskjed, eksternVarsling = true))
        testRapid.sendTestMessage(varselAktivertMessage(ident, beskjed, eksternVarsling = true))
        testRapid.sendTestMessage(varselAktivertMessage(ident, beskjed, eksternVarsling = true))
        testRapid.sendTestMessage(varselAktivertMessage(ident, beskjed, eksternVarsling = false))
        testRapid.sendTestMessage(varselAktivertMessage(ident, beskjed, eksternVarsling = false))

        database.antallVarsler(ident) shouldBe 5
        database.antallVarsler(ident, beskjed, eksternVarsling = true) shouldBe 3
        database.antallVarsler(ident, beskjed, eksternVarsling = false) shouldBe 2
    }

    @Test
    fun `teller for ulike dager`() {
        val ident = "12345"

        testRapid.sendTestMessage(varselAktivertMessage(ident, beskjed, forstBehandlet = LocalDateTimeHelper.nowAtUtc()))
        testRapid.sendTestMessage(varselAktivertMessage(ident, beskjed, forstBehandlet = LocalDateTimeHelper.nowAtUtc().minusDays(1)))
        testRapid.sendTestMessage(varselAktivertMessage(ident, beskjed, forstBehandlet = LocalDateTimeHelper.nowAtUtc().minusDays(1)))
        testRapid.sendTestMessage(varselAktivertMessage(ident, beskjed, forstBehandlet = LocalDateTimeHelper.nowAtUtc().minusDays(2)))
        testRapid.sendTestMessage(varselAktivertMessage(ident, beskjed, forstBehandlet = LocalDateTimeHelper.nowAtUtc().minusDays(2)))
        testRapid.sendTestMessage(varselAktivertMessage(ident, beskjed, forstBehandlet = LocalDateTimeHelper.nowAtUtc().minusDays(2)))

        database.antallVarsler(ident) shouldBe 6
        database.antallVarsler(ident, beskjed, LocalDate.now()) shouldBe 1
        database.antallVarsler(ident, beskjed, LocalDate.now().minusDays(1)) shouldBe 2
        database.antallVarsler(ident, beskjed, LocalDate.now().minusDays(2)) shouldBe 3
    }
}
