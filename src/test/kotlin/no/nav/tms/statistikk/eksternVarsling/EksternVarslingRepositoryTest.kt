package no.nav.tms.statistikk.eksternVarsling

import LocalPostgresDatabase
import assert
import cleanTables
import io.kotest.matchers.shouldBe
import no.nav.tms.statistikk.database.LocalDateTimeHelper
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class EksternVarslingRepositoryTest {

    private val db = LocalPostgresDatabase.cleanDb()
    private val repository = EksternVarslingRepository(db)

    @AfterEach
    fun cleanup() {
        db.cleanTables("innlogging_etter_eksternt_varsel")
    }

    @Test
    fun ` sett inn eksternt varsel`() {
        val sendtTime = LocalDateTimeHelper.nowAtUtc()
        repository.insertEksternVarsling("123", SMS, eksternVarslingTestIdent, sendtTime)
        db.getEksternVarsling("123").assert {
            size shouldBe 1
            first().assert {
                get("ident") shouldBe eksternVarslingTestIdent
                get("sms") shouldBe true
                get("epost") shouldBe false
                get("sendt") shouldBe sendtTime
            }
        }

        repository.insertEksternVarsling("123", EPOST, eksternVarslingTestIdent, sendtTime)

        db.getEksternVarsling("123").assert {
            size shouldBe 1
            first().assert {
                get("ident") shouldBe eksternVarslingTestIdent
                get("sms") shouldBe true
                get("epost") shouldBe true
                get("sendt") shouldBe sendtTime
            }
        }
    }

    @Test
    fun `revarsling`() {
        val lastVarselDate = LocalDateTimeHelper.nowAtUtc().minusDays(7)
        db.insertEksterntTestVarsel("123", eksternVarslingTestIdent, lastVarselDate, EPOST)
        val sendtTime = LocalDateTimeHelper.nowAtUtc()

        repository.insertEksternVarsling("123", EPOST, eksternVarslingTestIdent, sendtTime)

        db.getEksternVarsling("123").assert {
            size shouldBe 2
            first().assert {
                get("sendt").toDateTimeMinutes() shouldBe lastVarselDate.toMinutes()


            }
            last().assert {
                get("ident") shouldBe eksternVarslingTestIdent
                get("sms") shouldBe false
                get("epost") shouldBe true
                get("sendt") shouldBe sendtTime
            }
        }
    }
}

