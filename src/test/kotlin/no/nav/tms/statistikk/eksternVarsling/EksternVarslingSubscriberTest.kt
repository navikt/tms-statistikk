package no.nav.tms.statistikk.eksternVarsling

import LocalPostgresDatabase
import assert
import cleanTables
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import no.nav.tms.kafka.application.MessageBroadcaster
import no.nav.tms.statistikk.database.DateTimeHelper.nowAtUtcZ
import no.nav.tms.statistikk.varsel.*
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.util.*

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class EksternVarslingSubscriberTest {

    private val db = LocalPostgresDatabase.cleanDb()
    private val varselRepository = VarselRepository(db)

    private val broadcaster = MessageBroadcaster(listOf(
        EksternVarslingSubscriber(EksternVarslingRepository(db)),
        VarselAktivertSubscriber(varselRepository)
    ))

    @AfterEach
    fun cleanup() {
        db.cleanTables("innlogging_etter_eksternt_varsel")
    }

    @Test
    fun `oppdaterer status om ekstern varsling`() {
        val eventId1 = UUID.randomUUID().toString()
        val eventId2 = UUID.randomUUID().toString()
        val eventId3 = UUID.randomUUID().toString()

        broadcaster.broadcastJson(VarselTestData.varselAktivertMessage(varselId = eventId1, eksternVarsling = true))
        broadcaster.broadcastJson(SMS.testMessage(eventId1))

        broadcaster.broadcastJson(VarselTestData.varselAktivertMessage(varselId = eventId2, eksternVarsling = true))
        broadcaster.broadcastJson(EPOST.testMessage(eventId2))

        broadcaster.broadcastJson(VarselTestData.varselAktivertMessage(varselId = eventId3, eksternVarsling = true))
        broadcaster.broadcastJson(EPOST.testMessage(eventId3))
        broadcaster.broadcastJson(SMS.testMessage(eventId3))

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
    "ident": "$ident",
    "status": "sendt",
    "varselId": "$eventId",
    "varseltype": "oppgave",
    "produsent": {
        "cluster": "dev-gcp",
        "namespace": "pto",
        "appnavn": "veilarbaktivitet"                        
    },
    "kanal": "${this}",
    "tidspunkt": "${nowAtUtcZ()}"
}"""

private fun String.testMessageNew(eventId: String, ident: String = eksternVarslingTestIdent) = """{
    "@event_name": "eksternVarslingStatusOppdatert",
    "ident": "$ident",
    "status": "sendt",
    "varselId": "$eventId",
    "varseltype": "oppgave",
    "produsent": {
        "cluster": "dev-gcp",
        "namespace": "pto",
        "appnavn": "veilarbaktivitet"                        
    },
    "kanal": "${this}",
    "batch": true,
    "tidspunkt": "${nowAtUtcZ()}"
}"""
