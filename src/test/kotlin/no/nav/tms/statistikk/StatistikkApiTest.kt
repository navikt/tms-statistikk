package no.nav.tms.statistikk

import assert
import cleanTables
import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import io.kotest.matchers.shouldBe
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.testing.*
import kotliquery.queryOf
import no.nav.tms.statistikk.login.LoginRepository
import no.nav.tms.token.support.azure.validation.mock.installAzureAuthMock
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class StatistikkApiTest {

    private val db = LocalPostgresDatabase.cleanDb()
    private val loginRepository = LoginRepository(db)

    @BeforeEach
    fun cleanDb(){
        db.cleanTables("innlogging_per_dag")
    }

    @Test
    fun innlogging() = testApplication {
        application {
            statistikkApi(loginRepository, authorized)
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
            statistikkApi(loginRepository, unauthorized)
        }

        client.get("/hent").assert {
            status.shouldBe(HttpStatusCode.OK)
            headers["Content-Type"] shouldBe "text/html; charset=UTF-8"
        }
    }

    @Test
    fun `csv innlogging nedlasting`() = testApplication {

        loginRepository.registerLogin("1234576512")
        loginRepository.registerLogin("1234576515")
        loginRepository.registerLogin("1234576516")

        application {
            statistikkApi(loginRepository, unauthorized)
        }


        client.get("/hent/innlogging").assert {
            status.shouldBe(HttpStatusCode.OK)
            headers["Content-Type"] shouldBe "text/csv"
            headers["Content-Disposition"] shouldBe "attachment; filename=\"innlogging.csv\""
            csvReader().readAll(bodyAsText()).apply {
                size shouldBe 1
                first()[0] shouldBe "Innlogging etter ekstern varsling"
                first()[1].toInt() shouldBe 3
            }
        }
    }

    private val authorized: Application.() -> Unit = {
        installAzureAuthMock {
            setAsDefault = true
            alwaysAuthenticated = true
        }
    }

    private val unauthorized: Application.() -> Unit = {
        installAzureAuthMock {
            setAsDefault = true
            alwaysAuthenticated = false
        }
    }
}
