package no.nav.tms.statistikk

import no.nav.tms.common.util.config.StringEnvVar.getEnvVar

data class Environment(
    val jdbcUrl: String = jdbcUrl(),
    val groupId: String = getEnvVar("GROUP_ID"),

    val internVarselTopic: String = "min-side.brukervarsel-v1",
    val utkastTopic: String = "min-side.aapen-utkast-v1",
    val microfrontendTopic: String = "min-side.aapen-microfrontend-v1"
)


private fun jdbcUrl(): String {
    val host: String = getEnvVar("DB_HOST")
    val name: String = getEnvVar("DB_DATABASE")
    val user: String = getEnvVar("DB_USERNAME")
    val password: String = getEnvVar("DB_PASSWORD")

    return "jdbc:postgresql://${host}/$name?user=$user&password=$password"
}

