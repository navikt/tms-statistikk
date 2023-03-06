package no.nav.tms.statistikk.login

import LocalPostgresDatabase
import io.kotest.matchers.shouldBe
import kotliquery.queryOf
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.time.LocalDateTime

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class LoginRepositoryTest {

    private val db = LocalPostgresDatabase.cleanDb()
    private val loginRepository = LoginRepository(db)

    @Test
    fun registerLogin() {

        insertEksterntVarsel(eventId="0000-jjjj-9999", ident = "887795" )
        insertEksterntVarsel(eventId="0000-jjjj-9988", ident = "887796")

        loginRepository.registerLogin("887799")
        loginRepository.registerLogin("887796")
        loginRepository.registerLogin("887795")
        loginRepository.registerLogin("887797")
        //duplicate
        loginRepository.registerLogin("887799")
        insertEksterntVarsel(eventId="0000-jjjj-9977", ident = "887799")

        db.query {
            queryOf("SELECT COUNT(ident) as total FROM innlogging_per_dag")
                .map {
                    it.int("total")
                }.asSingle
        } shouldBe 4

        db.query {
            queryOf("SELECT COUNT(ident) as total FROM innlogging_etter_eksternt_varsel where innloggetTimestamp is not null")
                .map {
                    it.int("total")
                }.asSingle
        } shouldBe 2
    }

    private fun insertEksterntVarsel(eventId: String, ident: String) {

        db.update {
         queryOf("insert into innlogging_etter_eksternt_varsel values(:eventId,:ident,:nowTime)", mapOf("eventId" to eventId, "ident" to ident,"nowTime" to LocalDateTime.now()))
        }
    }
}

