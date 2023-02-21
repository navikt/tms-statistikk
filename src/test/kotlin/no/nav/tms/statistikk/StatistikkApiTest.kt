package no.nav.tms.statistikk

import assert
import io.kotest.matchers.shouldBe
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.server.testing.*
import kotliquery.queryOf
import no.nav.tms.statistikk.api.StatistikkPersistence
import org.junit.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class StatistikkApiTest {

    private val db = LocalPostgresDatabase.cleanDb()
    private val statsPersistence = StatistikkPersistence(db)

    @Test
    fun `innlogiingstatistikk`() = testApplication {
        application {
            statistikkApi(statsPersistence)
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
    fun `csv nedlasting`() = testApplication {
        application {
            statistikkApi(statsPersistence)
        }

        client.get("/hent").assert {
            status.shouldBe(HttpStatusCode.OK)
            headers["Content-Type"] shouldBe "text/html; charset=UTF-8"
        }
    }
}