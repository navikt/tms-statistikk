package no.nav.tms.statistikk.eksternVarsling

import LocalPostgresDatabase
import assert
import cleanTables
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.time.LocalDateTime
import java.time.ZoneId

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
        repository.insertEksternVarsling("123", SMS, eksternVarslingTestIdent)
        db.getEksternVarsling("123").assert {
            size shouldBe 1
            first().assert {
                get("ident") shouldBe eksternVarslingTestIdent
                get("sms") shouldBe true
                get("epost") shouldBe false
                get("sendt").toDateTimeMinutes() shouldBe LocalDateTime.now(ZoneId.of("UTC")).toMinutes()
            }
        }

        repository.insertEksternVarsling("123", EPOST, eksternVarslingTestIdent)

        db.getEksternVarsling("123").assert {
            size shouldBe 1
            first().assert {
                get("ident") shouldBe eksternVarslingTestIdent
                get("sms") shouldBe true
                get("epost") shouldBe true
                get("sendt").toDateTimeMinutes() shouldBe LocalDateTime.now(ZoneId.of("UTC")).toMinutes()
            }
        }
    }

    @Test
    fun `revarsling`() {
        val lastVarselDate = LocalDateTime.now(ZoneId.of("UTC")).minusDays(7)
        db.insertEksterntTestVarsel("123", eksternVarslingTestIdent, lastVarselDate, EPOST)

        repository.insertEksternVarsling("123", EPOST, eksternVarslingTestIdent)

        db.getEksternVarsling("123").assert {
            size shouldBe 2
            first().assert {
                get("sendt").toDateTimeMinutes() shouldBe lastVarselDate.toMinutes()


            }
            last().assert {
                get("ident") shouldBe eksternVarslingTestIdent
                get("sms") shouldBe false
                get("epost") shouldBe true
                get("sendt").toDateTimeMinutes() shouldBe LocalDateTime.now(ZoneId.of("UTC")).toMinutes()
            }
        }
    }
}

