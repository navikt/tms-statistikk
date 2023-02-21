package no.nav.tms.statistikk

import assert
import io.kotest.matchers.shouldBe
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.server.testing.*
import io.mockk.mockk
import no.nav.tms.statistikk.api.StatistikkPersistence
import org.junit.Test

internal class StatistikkApiTest {

    val persistanceInspector = object : TestInspector {
        override var loginStats = mutableListOf<String>()
        override var csvFetchCount = 0

        override fun updateLoginCount(ident: String) {
            loginStats.add(ident)
        }

        override fun getCSV(): String {
            csvFetchCount++
            return ""
        }
    }

    @Test
    fun `innlogiingstatistikk`() = testApplication {
        application {
            statistikkApi(mockk(relaxed = true), persistanceInspector)
        }

        client.post("/innlogging") {
            setBody("""{"ident":"123615426480"}""")
            headers {
                contentType(ContentType.Application.Json)
            }
        }.assert {
            status.shouldBe(HttpStatusCode.Created)
            persistanceInspector.loginStats.size shouldBe 1
        }
    }

    @Test
    fun `csv nedlasting`() = testApplication {
        application {
            statistikkApi(mockk(relaxed = true), persistanceInspector)
        }

        client.get("/hent").assert {
            status.shouldBe(HttpStatusCode.OK)
            persistanceInspector.csvFetchCount shouldBe 1
        }
    }
}

interface TestInspector : StatistikkPersistence {
    var loginStats: MutableList<String>
    var csvFetchCount: Int
}