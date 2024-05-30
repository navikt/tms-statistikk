package no.nav.tms.statistikk

import LocalPostgresDatabase
import io.kotest.matchers.shouldBe
import kotliquery.queryOf
import no.nav.tms.kafka.application.MessageBroadcaster
import no.nav.tms.statistikk.utkast.UtkastCreatedSubscriber
import no.nav.tms.statistikk.utkast.UtkastDeletedSubscriber
import no.nav.tms.statistikk.utkast.UtkastRespository
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import utkastCreatedMelding
import utkastDeletedMelding

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class UtkastSinkTest {
    private val db = LocalPostgresDatabase.cleanDb()
    private val utkastPersistence = UtkastRespository(db)
    private val testUtkastId = "997766530"
    private val testIdent = "887799"
    
    private val broadcaster = MessageBroadcaster(listOf(
        UtkastCreatedSubscriber(utkastPersistence),
        UtkastDeletedSubscriber(utkastPersistence)
    ))

    @BeforeAll
    fun setupSink() {
        broadcaster.broadcastJson(utkastCreatedMelding(testUtkastId, testIdent))
        broadcaster.broadcastJson(utkastCreatedMelding(testUtkastId, testIdent))
        broadcaster.broadcastJson(utkastCreatedMelding("667788", testIdent))
        broadcaster.broadcastJson(utkastCreatedMelding("5765", "967555"))
        broadcaster.broadcastJson(utkastCreatedMelding("5744465", "9699955"))
    }

    @Test
    fun `teller nye utkast`() {
        db.query {
            queryOf("select count(*) from utkast where time_created IS NOT NULL AND time_deleted IS NULL").map {
                it.int("count")
            }.asSingle
        } shouldBe 4
    }

    @Test
    fun `teller språk etter oppdatering`() {
   /*     broadcaster.broadcastJson(utkastUpdatedMelding(testUtkastId, testIdent))

        db.query {
            queryOf("select antall_språk from utkast where utkast_id='$testUtkastId'").map {
                it.int("antall_språk")
            }.asSingle
        } shouldBe 2*/

    }

    @Test
    fun `teller slettede utkast`() {
        broadcaster.broadcastJson(utkastCreatedMelding("00557711"))
        broadcaster.broadcastJson(utkastDeletedMelding("00557711"))
        broadcaster.broadcastJson(utkastDeletedMelding("00557711"))

        db.query {
            queryOf("select count(*) from utkast where time_deleted IS NOT NULL AND time_created IS NOT NULL").map {
                it.int("count")
            }.asSingle
        } shouldBe 1

        db.query {
            queryOf("select utkast_id, count(*) from utkast group by utkast_id ").map {
                it.int("count")
            }.asSingle
        } shouldBe  1

    }
}




