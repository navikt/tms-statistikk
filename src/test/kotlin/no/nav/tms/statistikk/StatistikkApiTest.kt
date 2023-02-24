package no.nav.tms.statistikk

import assert
import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import io.kotest.matchers.shouldBe
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.testing.*
import kotliquery.queryOf
import no.nav.tms.statistikk.api.StatistikkPersistence
import no.nav.tms.statistikk.login.LoginRepository
import no.nav.tms.token.support.azure.validation.mock.installAzureAuthMock
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class StatistikkApiTest {

    private val db = LocalPostgresDatabase.cleanDb()
    private val loginRepository = LoginRepository(db)
    private val persistence = StatistikkPersistence(db)

    @Test
    fun innlogging() = testApplication {
        application {
            statistikkApi(loginRepository, persistence, testAuth)
        }

        client.post("/innlogging") {
            setBody("""{"ident":"123615426480"}""")
            headers {
                contentType(ContentType.Application.Json)
            }
        }.status shouldBe(HttpStatusCode.NoContent)

        client.post("/innlogging") {
            setBody("""{"ident":"123615426480"}""")
            headers {
                contentType(ContentType.Application.Json)
            }
        }.status shouldBe(HttpStatusCode.NoContent)

        client.post("/innlogging") {
            setBody("""{"ident":"123615426489"}""")
            headers {
                contentType(ContentType.Application.Json)
            }
        }.status shouldBe(HttpStatusCode.NoContent)

        db.query {
            queryOf("SELECT COUNT(ident) as total FROM innlogging_per_dag")
                .map {
                    it.int("total")
                }.asSingle
        } shouldBe 2
    }

    @Test
    fun `Csv hente-side`() = testApplication {
        application {
            statistikkApi(loginRepository, persistence, testAuth)
        }

        client.get("/hent").assert {
            status.shouldBe(HttpStatusCode.OK)
            headers["Content-Type"] shouldBe "text/html; charset=UTF-8"
        }
    }

    @Test
    fun `csv fil nedlasting`() = testApplication {
        application {
            statistikkApi(loginRepository, persistence, testAuth)
        }

        client.get("/hent/lastned").assert {
            status.shouldBe(HttpStatusCode.OK)
            headers["Content-Type"] shouldBe "text/csv"
            val csvData = bodyAsText()
            csvReader().readAll(csvData).size shouldBe 2
        }
    }

    private val testAuth: Application.() -> Unit = {
        installAzureAuthMock {
            setAsDefault = true
            alwaysAuthenticated = true
        }
    }
}
