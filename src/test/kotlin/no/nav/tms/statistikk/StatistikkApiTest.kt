package no.nav.tms.statistikk

import assert
import io.kotest.matchers.shouldBe
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.server.testing.*
import io.mockk.mockk
import no.nav.tms.statistikk.api.StatistikkPersistence
import no.nav.tms.statistikk.database.Database
import org.junit.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class StatistikkApiTest {

    val persistanceInspector = StatistikkPersistence(LocalPostgresDatabase.cleanDb())

    @Test
    fun `innlogiingstatistikk`() = testApplication {
        application {
            statistikkApi(persistanceInspector)
        }

        client.post("/innlogging") {
            setBody("""{"ident":"123615426480"}""")
            headers {
                contentType(ContentType.Application.Json)
            }
        }.assert {
            status.shouldBe(HttpStatusCode.Created)
        }
    }

    @Test
    fun `csv nedlasting`() = testApplication {
        application {
            statistikkApi(persistanceInspector)
        }

        client.get("/hent").assert {
            status.shouldBe(HttpStatusCode.OK)
        }
    }
}