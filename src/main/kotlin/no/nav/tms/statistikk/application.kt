package no.nav.tms.statistikk

import no.nav.helse.rapids_rivers.RapidApplication
import no.nav.helse.rapids_rivers.RapidApplication.RapidApplicationConfig.Companion.fromEnv
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.tms.statistikk.api.StatistikkPersistence
import no.nav.tms.statistikk.database.Flyway

fun main() {
    val environment = Environment()
    startRapid(
        environment = environment,
    )
}

private fun startRapid(
    environment: Environment,
) {
    RapidApplication.Builder(fromEnv(environment.rapidConfig())).withKtorModule {
        statistikkApi(tmpPersistance)
    }.build().apply {
        VarselPerDagSink(this)
    }.apply {
        register(object : RapidsConnection.StatusListener {
            override fun onStartup(rapidsConnection: RapidsConnection) {
                Flyway.runFlywayMigrations(environment)
            }
        })
    }.start()
}

val tmpPersistance = object : StatistikkPersistence {
    override fun updateLoginCount(ident: String) {
        TODO("Not yet implemented")
    }

    override fun getCSV(): String {
        TODO("Not yet implemented")
    }
}
