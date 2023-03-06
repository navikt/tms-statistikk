package no.nav.tms.statistikk

import LocalPostgresDatabase
import io.kotest.matchers.shouldBe
import kotliquery.queryOf
import no.nav.helse.rapids_rivers.testsupport.TestRapid
import no.nav.tms.statistikk.utkast.UtkastPersistance
import no.nav.tms.statistikk.utkast.UtkastSink
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import utkastCreatedMelding
import utkastDeletedMelding
import utkastUpdatedMelding

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class UtkastSinkTest {
    private val db = LocalPostgresDatabase.cleanDb()
    private val utkastPersistence = UtkastPersistance(db)
    private val testRapid = TestRapid()
    private val testUtkastId = "997766530"
    private val testIdent = "887799"

    @BeforeAll
    fun setupSink() {
        UtkastSink(testRapid, utkastPersistence)
        testRapid.sendTestMessage(utkastCreatedMelding(testUtkastId, testIdent))
        testRapid.sendTestMessage(utkastCreatedMelding("667788", testIdent))
        testRapid.sendTestMessage(utkastCreatedMelding("5765", "967555"))
        testRapid.sendTestMessage(utkastCreatedMelding("5744465", "9699955"))
    }

    @Test
    fun `teller nye utkast`() {
        db.query {
            queryOf("select count(*) from utkast where event='created'").map {
                it.int("count")
            }.asSingle
        } shouldBe 4
    }

    @Test
    fun `teller oppdaterte utkast`() {
        testRapid.sendTestMessage(utkastUpdatedMelding(testUtkastId, testIdent))

        db.query {
            queryOf("select antall_språk from utkast where utkast_id='$testUtkastId'").map {
                it.int("antall_språk")
            }.asSingle
        } shouldBe 2

    }

    @Test
    fun `teller slettede utkast`() {
        testRapid.sendTestMessage(utkastDeletedMelding(testUtkastId))

        db.query {
            queryOf("select count(*) from utkast where event='deleted'").map {
                it.int("count")
            }.asSingle
        } shouldBe 1

    }
}




