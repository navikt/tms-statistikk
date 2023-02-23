package no.nav.tms.statistikk

import no.nav.helse.rapids_rivers.RapidApplication
import no.nav.helse.rapids_rivers.RapidApplication.RapidApplicationConfig.Companion.fromEnv
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.tms.statistikk.api.StatistikkPersistence
import no.nav.tms.statistikk.database.Database
import no.nav.tms.statistikk.database.Flyway
import no.nav.tms.statistikk.database.PostgresDatabase
import no.nav.tms.statistikk.varsel.VarselPerDagSink
import no.nav.tms.statistikk.varsel.VarselRepository
import no.nav.tms.statistikk.database.PostgresDatabase

fun main() {
    val environment = Environment()
    startRapid(
        environment = environment,
        database = PostgresDatabase(environment)
    )
}

private fun startRapid(
    environment: Environment,
    database: Database
) {
    val database = PostgresDatabase(environment)

    val varselRepository = VarselRepository(database)

    RapidApplication.Builder(fromEnv(environment.rapidConfig())).withKtorModule {
        statistikkApi(database)
    }.build().apply {
        VarselPerDagSink(this, varselRepository)
    }.apply {
        register(object : RapidsConnection.StatusListener {
            override fun onStartup(rapidsConnection: RapidsConnection) {
                Flyway.runFlywayMigrations(environment)
            }
        })
    }.start()
}
