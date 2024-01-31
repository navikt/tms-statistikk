package no.nav.tms.statistikk.varsel

import assert
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import kotliquery.queryOf
import no.nav.helse.rapids_rivers.testsupport.TestRapid
import no.nav.tms.statistikk.database.DateTimeHelper
import no.nav.tms.statistikk.varsel.VarselTestData.VarselType.beskjed
import no.nav.tms.statistikk.varsel.VarselTestData.addBeredskapTittel
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
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
        database.update { queryOf("delete from beredskapsvarsel") }
        database.update { queryOf("delete from varsel") }
    }

    @Test
    fun `legger til varsler i db`() {
        val ident = "123"
        val type = beskjed
        val eventId = UUID.randomUUID().toString()
        val tekst = "abcdef"
        val sensitivitet = Sensitivitet.High
        val link = "http://link"
        val namespace = "ns"
        val appnavn = "app"
        val forstBehandlet = DateTimeHelper.nowAtUtcZ()
        val synligFremTil = null
        val eksternVarsling = true

        val testMessage = VarselTestData.varselAktivertMessage(
            ident = ident,
            type = type,
            varselId = eventId,
            tekst = tekst,
            sensitivitet = sensitivitet,
            link = link,
            namespace = namespace,
            appnavn = appnavn,
            opprettet = forstBehandlet,
            aktivFremTil = synligFremTil,
            eksternVarsling = eksternVarsling
        )

        testRapid.sendTestMessage(testMessage)

        val varsel = database.getVarsel(eventId)

        varsel shouldNotBe null
        varsel!!
        varsel.varselId shouldBe eventId
        varsel.ident shouldBe ident
        varsel.type shouldBe beskjed.name
        varsel.namespace shouldBe namespace
        varsel.appnavn shouldBe appnavn
        varsel.tekstlengde shouldBe tekst.length
        varsel.lenke shouldBe true
        varsel.sikkerhetsnivaa shouldBe sensitivitet.loginLevel
        varsel.aktiv shouldBe true
        varsel.forstBehandlet shouldBe forstBehandlet.toLocalDateTime()
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

        val tomLenkeVarsel = VarselTestData.varselAktivertMessage(varselId = eventId1, link = " ")
        val varselMedLenke = VarselTestData.varselAktivertMessage(varselId = eventId2, link = "http://link")

        testRapid.sendTestMessage(tomLenkeVarsel)
        testRapid.sendTestMessage(varselMedLenke)

        database.getVarsel(eventId1)?.lenke shouldBe false
        database.getVarsel(eventId2)?.lenke shouldBe true
    }

    @Test
    fun `lagrer beredskapstittel`() {
        val eventId1 = UUID.randomUUID().toString()
        val eventId2 = UUID.randomUUID().toString()

        val medBeredskapsTittel =
            VarselTestData.varselAktivertMessage(varselId = eventId1).addBeredskapTittel("Something happened")
        val utenBeredskapsTittel = VarselTestData.varselAktivertMessage(varselId = eventId2)

        testRapid.sendTestMessage(utenBeredskapsTittel)
        testRapid.sendTestMessage(medBeredskapsTittel)

        database.getVarsel(eventId2).assert {
            require(this != null)
            beredskapstittel shouldBe null
        }

        database.getVarsel(eventId1).assert {
            require(this != null)
            beredskapstittel shouldBe "Something happened"
        }
    }

    @Test
    fun `tolker frist riktig`() {
        val eventId1 = UUID.randomUUID().toString()
        val eventId2 = UUID.randomUUID().toString()

        val utenFrist = VarselTestData.varselAktivertMessage(varselId = eventId1, aktivFremTil = null)
        val medFrist =
            VarselTestData.varselAktivertMessage(varselId = eventId2, aktivFremTil = DateTimeHelper.nowAtUtcZ())

        testRapid.sendTestMessage(utenFrist)
        testRapid.sendTestMessage(medFrist)

        database.getVarsel(eventId1)?.frist shouldBe false
        database.getVarsel(eventId2)?.frist shouldBe true
    }
}
