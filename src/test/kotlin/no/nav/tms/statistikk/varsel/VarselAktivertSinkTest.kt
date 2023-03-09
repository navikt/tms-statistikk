package no.nav.tms.statistikk.varsel

import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import kotliquery.queryOf
import no.nav.helse.rapids_rivers.testsupport.TestRapid
import no.nav.tms.statistikk.varsel.VarselTestData.VarselType.beskjed
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.util.*

internal class VarselAktivertSinkTest {
    private val database = LocalPostgresDatabase.cleanDb()
    private val varselRepository = VarselRepository(database)
    private val testRapid = TestRapid()

    @BeforeAll
    fun setupSinks() {
        VarselAktivertSink(testRapid, varselRepository)
    }

    @AfterEach
    fun cleanDb() {
        database.update { queryOf("delete from varsel") }
    }

    @Test
    fun `legger til varsler i db`() {
        val ident = "123"
        val type = beskjed
        val eventId = UUID.randomUUID().toString()
        val tekst = "abcdef"
        val sikkerhetsnivaa = 4
        val link = "http://link"
        val namespace = "ns"
        val appnavn = "app"
        val forstBehandlet = LocalDateTime.now()
        val synligFremTil = null
        val eksternVarsling = true

        val testMessage = VarselTestData.varselAktivertMessage(
            ident = ident,
            type = type,
            eventId = eventId,
            tekst = tekst,
            sikkerhetsnivaa = sikkerhetsnivaa,
            link = link,
            namespace = namespace,
            appnavn = appnavn,
            forstBehandlet = forstBehandlet,
            synligFremTil = synligFremTil,
            eksternVarsling = eksternVarsling
        )

        testRapid.sendTestMessage(testMessage)

        val varsel = database.getVarsel(eventId)

        varsel shouldNotBe null
        varsel!!
        varsel.eventId shouldBe eventId
        varsel.ident shouldBe ident
        varsel.type shouldBe beskjed.name
        varsel.namespace shouldBe namespace
        varsel.appnavn shouldBe appnavn
        varsel.tekstlengde shouldBe tekst.length
        varsel.lenke shouldBe true
        varsel.sikkerhetsnivaa shouldBe sikkerhetsnivaa
        varsel.aktiv shouldBe true
        varsel.forstBehandlet shouldBe forstBehandlet
        varsel.frist shouldBe false
        varsel.inaktivertTidspunkt shouldBe null
        varsel.inaktivertKilde shouldBe null
        varsel.eksternVarslingBestilt shouldBe eksternVarsling
        varsel.eksternVarslingSendtSms shouldBe false
        varsel.eksternVarslingSendtEpost shouldBe false
    }

    @Test
    fun `tolker lenke-felt riktig`() {
        val eventId1 = UUID.randomUUID().toString()
        val eventId2 = UUID.randomUUID().toString()

        val tomLenkeVarsel = VarselTestData.varselAktivertMessage(eventId = eventId1, link = " ")
        val varselMedLenke = VarselTestData.varselAktivertMessage(eventId = eventId2, link = "http://link")

        testRapid.sendTestMessage(tomLenkeVarsel)
        testRapid.sendTestMessage(varselMedLenke)

        database.getVarsel(eventId1)?.lenke shouldBe false
        database.getVarsel(eventId2)?.lenke shouldBe true
    }

    @Test
    fun `tolker frist riktig`() {
        val eventId1 = UUID.randomUUID().toString()
        val eventId2 = UUID.randomUUID().toString()

        val utenFrist = VarselTestData.varselAktivertMessage(eventId = eventId1, synligFremTil = null)
        val medFrist = VarselTestData.varselAktivertMessage(eventId = eventId2, synligFremTil = LocalDateTime.now())

        testRapid.sendTestMessage(utenFrist)
        testRapid.sendTestMessage(medFrist)

        database.getVarsel(eventId1)?.frist shouldBe false
        database.getVarsel(eventId2)?.frist shouldBe true
    }
}
