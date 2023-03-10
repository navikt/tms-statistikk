package no.nav.tms.statistikk

import no.nav.helse.rapids_rivers.RapidApplication
import no.nav.helse.rapids_rivers.RapidApplication.RapidApplicationConfig.Companion.fromEnv
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.tms.statistikk.database.Flyway
import no.nav.tms.statistikk.database.PostgresDatabase
import no.nav.tms.statistikk.login.LoginRepository
import no.nav.tms.statistikk.varsel.*

fun main() {
    val environment = Environment()
    startRapid(
        environment = environment
    )
}

private fun startRapid(
    environment: Environment,
) {
    val database = PostgresDatabase(environment)

    val loginRepository = LoginRepository(database)
    val varselRepository = VarselRepository(database)

    RapidApplication.Builder(fromEnv(environment.rapidConfig())).withKtorModule {
        statistikkApi(loginRepository)
    }.build().apply {
        VarselAktivertSink(this, varselRepository)
        VarselInaktivertSink(this, varselRepository)
        EksternVarslingSendtSink(this, varselRepository)
        VarselPerDagSink(this, varselRepository)
    }.apply {
        register(object : RapidsConnection.StatusListener {
            override fun onStartup(rapidsConnection: RapidsConnection) {
                Flyway.runFlywayMigrations(environment)
            }
        })
    }.start()
}
