package no.nav.tms.statistikk

import io.micrometer.prometheus.PrometheusConfig
import io.micrometer.prometheus.PrometheusMeterRegistry
import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.RapidApplication
import no.nav.helse.rapids_rivers.RapidApplication.RapidApplicationConfig.Companion.fromEnv
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.tms.statistikk.api.StatistikkPersistence
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
        statistikkApi(prometheusMeterRegistry, tmpPersistance)
    }.build().apply {

    }.apply {
        register(object : RapidsConnection.StatusListener {
            override fun onStartup(rapidsConnection: RapidsConnection) {
                Flyway.runFlywayMigrations(environment)
            }
        })
    }.start()
}

val JsonMessage.ident: String
    get() {
        return get("ident").asText()
    }
val JsonMessage.microfrontendId: String
    get() {
        return get("microfrontend_id").asText()
    }

val tmpPersistance = object : StatistikkPersistence {
    override fun updateLoginCount(ident: String) {
        TODO("Not yet implemented")
    }

    override fun getCSV(): String {
        TODO("Not yet implemented")
    }

}