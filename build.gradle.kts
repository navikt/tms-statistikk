import org.gradle.api.tasks.testing.logging.TestExceptionFormat

plugins {
    kotlin("jvm").version(Kotlin.version)

    id(Shadow.pluginId) version (Shadow.version)

    application
}

kotlin {
    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

repositories {
    mavenCentral()
    maven {
        url = uri("https://github-package-registry-mirror.gc.nav.no/cached/maven-release")
    }
    mavenLocal()
}

dependencies {
    implementation(Flyway.core)
    implementation(Flyway.postgres)
    implementation(Hikari.cp)
    implementation(JacksonDatatype.datatypeJsr310)
    implementation(JacksonDatatype.moduleKotlin)
    implementation(KotlinLogging.logging)
    implementation(Ktor.Server.core)
    implementation(Ktor.Server.netty)
    implementation(Ktor.Server.contentNegotiation)
    implementation(Ktor.Server.auth)
    implementation(Ktor.Server.authJwt)
    implementation(Ktor.Server.statusPages)
    implementation(Ktor.Serialization.jackson)
    implementation(KtorHtml.htmlBuilder)
    implementation(Logstash.logbackEncoder)
    implementation(Micrometer.registryPrometheus)
    implementation(TmsCommonLib.utils)
    implementation(TmsKtorTokenSupport.azureValidation)
    implementation(Postgresql.postgresql)
    implementation(KotliQuery.kotliquery)
    implementation(TmsKafkaTools.kafkaApplication)

    testImplementation(Junit.api)
    testImplementation(Junit.engine)
    testImplementation(TestContainers.postgresql)
    testImplementation(Kotest.runnerJunit5)
    testImplementation(Kotest.assertionsCore)
    testImplementation(Ktor.Test.serverTestHost)
    testImplementation(TmsKtorTokenSupport.azureValidationMock)
    testImplementation(Mockk.mockk)
}

application {
    mainClass.set("no.nav.tms.statistikk.ApplicationKt")
}

tasks {
    shadowJar {
        mergeServiceFiles()
    }
}

tasks {
    withType<Test> {
        useJUnitPlatform()
        testLogging {
            exceptionFormat = TestExceptionFormat.FULL
            events("passed", "skipped", "failed")
        }
    }
}
