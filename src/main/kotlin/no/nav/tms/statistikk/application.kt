package no.nav.tms.statistikk

import no.nav.tms.kafka.application.KafkaApplication
import no.nav.tms.statistikk.database.Flyway
import no.nav.tms.statistikk.database.PostgresDatabase
import no.nav.tms.statistikk.eksternVarsling.EksternVarslingRepository
import no.nav.tms.statistikk.eksternVarsling.EksternVarslingSubscriber
import no.nav.tms.statistikk.login.LoginRepository
import no.nav.tms.statistikk.microfrontends.MicrofrontendRepository
import no.nav.tms.statistikk.microfrontends.MicrofrontendSubscriber
import no.nav.tms.statistikk.utkast.*
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
    val eksternVarslingRepository = EksternVarslingRepository(database)
    val utkastRespository = UtkastRespository(database)

    KafkaApplication.build {
        kafkaConfig {
            groupId = environment.groupId
            readTopics(
                "min-side.brukervarsel-v1",
                "min-side.aapen-utkast-v1",
                "min-side.aapen-microfrontend-v1"
            )
            eventNameFields("@event_name", "@action")
        }

        ktorModule {
            statistikkApi(loginRepository)
        }

        subscribers(
            VarselAktivertSubscriber(varselRepository),
            VarselInaktivertSubscriber(varselRepository),
            VarselPerDagSubscriber(varselRepository),
            EksternVarslingSubscriber(eksternVarslingRepository),
            UtkastCreatedSubscriber(utkastRespository),
            UtkastDeletedSubscriber(utkastRespository),
            MicrofrontendSubscriber(MicrofrontendRepository(database))
        )

        onStartup {
            Flyway.runFlywayMigrations(environment)
        }
    }.start()
}
