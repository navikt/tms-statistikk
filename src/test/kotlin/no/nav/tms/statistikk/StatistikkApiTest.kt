package no.nav.tms.statistikk

import assert
import cleanTables
import io.kotest.matchers.shouldBe
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.testing.*
import kotliquery.queryOf
import no.nav.tms.token.support.azure.validation.mock.azureMock
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class StatistikkApiTest {

    private val db = LocalPostgresDatabase.cleanDb()

    @BeforeEach
    fun cleanDb(){
        db.cleanTables("innlogging_per_dag")
    }

    @Test
    fun `lagrer ikke data om innlogging`() = testApplication {
        application {
            statistikkApi(authorized)
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
        } shouldBe 0

        db.query {
            queryOf("SELECT COUNT(ident) as total FROM innlogging_etter_eksternt_varsel")
                .map {
                    it.int("total")
                }.asSingle
        } shouldBe 0
    }

    private val authorized: Application.() -> Unit = {
        authentication {
            azureMock {
                setAsDefault = true
                alwaysAuthenticated = true
            }
        }
    }

    private val unauthorized: Application.() -> Unit = {
        authentication {
            azureMock {
                setAsDefault = true
                alwaysAuthenticated = false
            }
        }
    }
}
