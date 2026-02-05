package no.nav.tms.statistikk.varsel

import io.kotest.matchers.shouldBe
import kotliquery.queryOf
import no.nav.tms.kafka.application.MessageBroadcaster
import no.nav.tms.statistikk.database.DateTimeHelper
import no.nav.tms.statistikk.varsel.VarselTestData.addBeredskapMetadata
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.util.*

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class VarselInaktivertSubscriberTest {
    private val database = LocalPostgresDatabase.getCleanInstance()
    private val varselRepository = VarselRepository(database)

    private val broadcaster = MessageBroadcaster(listOf(
        VarselAktivertSubscriber(varselRepository),
        VarselInaktivertSubscriber(varselRepository)
    ))

    @AfterEach
    fun cleanDb() {
        database.update { queryOf("delete from beredskapsvarsel") }
        database.update { queryOf("delete from varsel") }
    }

    @Test
    fun `registrerer at varsel har blitt inaktivert`() {
        val eventId = UUID.randomUUID().toString()
        val kilde = "bruker"
        val inaktivertTidspunkt = DateTimeHelper.nowAtUtcZ().minusHours(1)

        broadcaster.broadcastJson(VarselTestData.varselAktivertMessage(varselId = eventId).addBeredskapMetadata("oups"))
        broadcaster.broadcastJson(
            VarselTestData.varselInaktivertMessage(
                varselId = eventId,
                kilde = kilde,
                tidspunkt = inaktivertTidspunkt
            )
        )

        val varsel = database.getVarsel(eventId)!!

        varsel.aktiv shouldBe false
        varsel.inaktivertTidspunkt shouldBe inaktivertTidspunkt.toLocalDateTime()
        varsel.inaktivertKilde shouldBe kilde
        varsel.beredskapstittel shouldBe "oups"
        varsel.beredskapsRef shouldBe "123"
    }
}
