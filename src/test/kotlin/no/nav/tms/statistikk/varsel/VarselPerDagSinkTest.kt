package no.nav.tms.statistikk.varsel

import io.micrometer.prometheus.PrometheusConfig
import io.micrometer.prometheus.PrometheusMeterRegistry
import kotliquery.queryOf
import no.nav.helse.rapids_rivers.testsupport.TestRapid
import no.nav.tms.statistikk.varsel.VarselTestData.VarselType.*
import no.nav.tms.statistikk.varsel.VarselTestData.varselAktivertMessage
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test

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
        testRapid.sendTestMessage(varselAktivertMessage(ident, oppgave))
        testRapid.sendTestMessage(varselAktivertMessage(ident, innboks))


    }
}
