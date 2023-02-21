package no.nav.tms.statistikk

import no.nav.personbruker.dittnav.common.util.config.StringEnvVar.getEnvVar

data class Environment(
    val groupId: String = getEnvVar("GROUP_ID"),
    val varselTopic: String = getEnvVar("VARSEL_TOPIC"),
    val dbUrl: String = getDbUrl(),
    val dbUser: String = getEnvVar("DB_USERNAME"),
    val dbPassword: String = getEnvVar("DB_PASSWORD"),
    val kafkaBrokers: String = getEnvVar("KAFKA_BROKERS"),
    val kafkaTruststorePath: String = getEnvVar("KAFKA_TRUSTSTORE_PATH"),
    val kafkaKeystorePath: String = getEnvVar("KAFKA_KEYSTORE_PATH"),
    val kafkaCredstorePassword: String = getEnvVar("KAFKA_CREDSTORE_PASSWORD"),
    val kafkaSchemaRegistryUser: String = getEnvVar("KAFKA_SCHEMA_REGISTRY_USER"),
    val kafkaSchemaRegistryPassword: String = getEnvVar("KAFKA_SCHEMA_REGISTRY_PASSWORD"),
    ) {
    fun rapidConfig(): Map<String, String> = mapOf(
        "KAFKA_BROKERS" to kafkaBrokers,
        "KAFKA_CONSUMER_GROUP_ID" to groupId,
        "KAFKA_RAPID_TOPIC" to varselTopic,
        "KAFKA_KEYSTORE_PATH" to kafkaKeystorePath,
        "KAFKA_CREDSTORE_PASSWORD" to kafkaCredstorePassword,
        "KAFKA_TRUSTSTORE_PATH" to kafkaTruststorePath,
        "KAFKA_RESET_POLICY" to "earliest",
        "HTTP_PORT" to "8080"
    )
}

fun getDbUrl(
    host: String = getEnvVar("DB_HOST"),
    port: String = getEnvVar("DB_PORT"),
    name: String = getEnvVar("DB_DATABASE")
): String {
    return if (host.endsWith(":$port")) {
        "jdbc:postgresql://${host}/$name"
    } else {
        "jdbc:postgresql://${host}:${port}/${name}"
    }
}