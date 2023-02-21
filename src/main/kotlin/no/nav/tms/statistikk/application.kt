package no.nav.tms.statistikk

import io.micrometer.prometheus.PrometheusConfig
import io.micrometer.prometheus.PrometheusMeterRegistry
import no.nav.helse.rapids_rivers.RapidApplication
import no.nav.helse.rapids_rivers.RapidApplication.RapidApplicationConfig.Companion.fromEnv
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.tms.statistikk.database.Flyway

fun main() {
    val environment = Environment()

    startRapid(
        environment = environment,
        prometheusMeterRegistry = PrometheusMeterRegistry(PrometheusConfig.DEFAULT)
    )
}

private fun startRapid(
    environment: Environment,
    prometheusMeterRegistry: PrometheusMeterRegistry,
) {
    RapidApplication.Builder(fromEnv(environment.rapidConfig())).withKtorModule {
        statistikkApi(prometheusMeterRegistry)
    }.build().apply {

    }.apply {
        register(object : RapidsConnection.StatusListener {
            override fun onStartup(rapidsConnection: RapidsConnection) {
                Flyway.runFlywayMigrations(environment)
            }
        })
    }.start()
}
