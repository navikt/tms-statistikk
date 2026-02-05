package no.nav.tms.statistikk.varsel

import io.kotest.matchers.shouldBe
import kotliquery.queryOf
import no.nav.tms.kafka.application.MessageBroadcaster
import no.nav.tms.statistikk.database.DateTimeHelper
import no.nav.tms.statistikk.varsel.VarselTestData.VarselType.*
import no.nav.tms.statistikk.varsel.VarselTestData.varselAktivertMessage
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import java.time.LocalDate

class VarselPerDagSubscriberTest {

    private val database = LocalPostgresDatabase.getCleanInstance()
    private val varselRepository = VarselRepository(database)

    private val broadcaster = MessageBroadcaster(listOf(
        VarselPerDagSubscriber(varselRepository)
    ))

    @AfterEach
    fun cleanDb() {
        database.update { queryOf("delete from varsler_per_dag") }
    }

    @Test
    fun `teller opp ulike typer varsler`() {
        val ident = "12345"

        broadcaster.broadcastJson(varselAktivertMessage(ident, beskjed))
        broadcaster.broadcastJson(varselAktivertMessage(ident, beskjed))
        broadcaster.broadcastJson(varselAktivertMessage(ident, beskjed))
        broadcaster.broadcastJson(varselAktivertMessage(ident, oppgave))
        broadcaster.broadcastJson(varselAktivertMessage(ident, oppgave))
        broadcaster.broadcastJson(varselAktivertMessage(ident, innboks))

        database.antallVarsler(ident) shouldBe 6
        database.antallVarsler(ident, beskjed) shouldBe 3
        database.antallVarsler(ident, oppgave) shouldBe 2
        database.antallVarsler(ident, innboks) shouldBe 1
    }

    @Test
    fun `teller ekstern varsling eller ikke`() {
        val ident = "12345"

        broadcaster.broadcastJson(varselAktivertMessage(ident, beskjed, eksternVarsling = true))
        broadcaster.broadcastJson(varselAktivertMessage(ident, beskjed, eksternVarsling = true))
        broadcaster.broadcastJson(varselAktivertMessage(ident, beskjed, eksternVarsling = true))
        broadcaster.broadcastJson(varselAktivertMessage(ident, beskjed, eksternVarsling = false))
        broadcaster.broadcastJson(varselAktivertMessage(ident, beskjed, eksternVarsling = false))

        database.antallVarsler(ident) shouldBe 5
        database.antallVarsler(ident, beskjed, eksternVarsling = true) shouldBe 3
        database.antallVarsler(ident, beskjed, eksternVarsling = false) shouldBe 2
    }

    @Test
    fun `teller for ulike dager`() {
        val ident = "12345"

        broadcaster.broadcastJson(varselAktivertMessage(ident, beskjed, opprettet = DateTimeHelper.nowAtUtcZ()))
        broadcaster.broadcastJson(varselAktivertMessage(ident, beskjed, opprettet = DateTimeHelper.nowAtUtcZ().minusDays(1)))
        broadcaster.broadcastJson(varselAktivertMessage(ident, beskjed, opprettet = DateTimeHelper.nowAtUtcZ().minusDays(1)))
        broadcaster.broadcastJson(varselAktivertMessage(ident, beskjed, opprettet = DateTimeHelper.nowAtUtcZ().minusDays(2)))
        broadcaster.broadcastJson(varselAktivertMessage(ident, beskjed, opprettet = DateTimeHelper.nowAtUtcZ().minusDays(2)))
        broadcaster.broadcastJson(varselAktivertMessage(ident, beskjed, opprettet = DateTimeHelper.nowAtUtcZ().minusDays(2)))

        database.antallVarsler(ident) shouldBe 6
        database.antallVarsler(ident, beskjed, LocalDate.now()) shouldBe 1
        database.antallVarsler(ident, beskjed, LocalDate.now().minusDays(1)) shouldBe 2
        database.antallVarsler(ident, beskjed, LocalDate.now().minusDays(2)) shouldBe 3
    }
}
