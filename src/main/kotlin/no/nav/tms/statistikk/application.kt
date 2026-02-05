package no.nav.tms.statistikk

import no.nav.tms.common.postgres.Postgres
import no.nav.tms.kafka.application.KafkaApplication
import no.nav.tms.statistikk.eksternVarsling.EksternVarslingRepository
import no.nav.tms.statistikk.eksternVarsling.EksternVarslingSubscriber
import no.nav.tms.statistikk.microfrontends.MicrofrontendRepository
import no.nav.tms.statistikk.microfrontends.MicrofrontendSubscriber
import no.nav.tms.statistikk.utkast.*
import no.nav.tms.statistikk.varsel.*
import org.flywaydb.core.Flyway

fun main() {
    val environment = Environment()

    val database = Postgres.connectToJdbcUrl(environment.jdbcUrl)

    val varselRepository = VarselRepository(database)
    val eksternVarslingRepository = EksternVarslingRepository(database)
    val utkastRespository = UtkastRespository(database)

    KafkaApplication.build {
        kafkaConfig {
            groupId = environment.groupId
            readTopics(
                environment.internVarselTopic,
                environment.utkastTopic ,
                environment.microfrontendTopic,
            )
            eventNameFields("@event_name", "@action")
        }

        ktorModule {
            statistikkApi()
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
            Flyway.configure()
                .dataSource(database.dataSource)
                .load()
                .migrate()
        }
    }.start()
}
