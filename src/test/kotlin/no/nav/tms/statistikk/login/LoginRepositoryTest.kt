package no.nav.tms.statistikk.login

import LocalPostgresDatabase
import io.kotest.matchers.shouldBe
import kotliquery.queryOf
import no.nav.tms.statistikk.eksternVarsling.Kanal
import no.nav.tms.statistikk.eksternVarsling.insertTestEksterntVarsel
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance


@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class LoginRepositoryTest {

    private val db = LocalPostgresDatabase.cleanDb()
    private val loginRepository = LoginRepository(db)

    @Test
    fun registerLogin() {

        db.insertTestEksterntVarsel(eventId="0000-jjjj-9999", ident = "887795", kanal = Kanal.EPOST)
        db.insertTestEksterntVarsel(eventId="0000-jjjj-9988", ident = "887796", kanal = Kanal.EPOST)

        loginRepository.registerLogin("887799")
        loginRepository.registerLogin("887796")
        loginRepository.registerLogin("887795")
        loginRepository.registerLogin("887797")
        //duplicate
        loginRepository.registerLogin("887799")
        db.insertTestEksterntVarsel(eventId="0000-jjjj-9977", ident = "887799", kanal = Kanal.EPOST)

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
}

