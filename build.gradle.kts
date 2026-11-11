import org.gradle.api.tasks.testing.logging.TestExceptionFormat

plugins {
    alias(managed.plugins.kotlin)
    alias(managed.plugins.tms.jarBundling)
    application
}

jarBundling {
    applicationName = "app"
    outputDirectory = "libs"
}

kotlin {
    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
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
    implementation(managed.bundles.flyway)
    implementation(managed.hikariCp)
    implementation(managed.jackson.datatypeJsr310)
    implementation(managed.jackson.moduleKotlin)
    implementation(managed.kotlinLogging)
    implementation(managed.ktor.server.core)
    implementation(managed.ktor.server.netty)
    implementation(managed.ktor.server.contentNegotiation)
    implementation(managed.ktor.server.auth)
    implementation(managed.ktor.server.auth.jwt)
    implementation(managed.ktor.server.statusPages)
    implementation(managed.ktor.serialization.jackson)
    implementation(managed.logback.encoder)
    implementation(managed.postgresql)
    implementation(managed.kotliQuery)
    implementation(managed.tms.commonLib.teamLogger)
    implementation(managed.tms.commonLib.utils)
    implementation(managed.tms.kafka.application)
    implementation(managed.tms.ktorTokenSupport.entraId.verification)

    testImplementation(managed.junit.launcher)
    testImplementation(managed.junit.api)
    testImplementation(managed.junit.engine)
    testImplementation(managed.testContainers.postgresql)
    testImplementation(managed.kotest.assertion)
    testImplementation(managed.ktor.test.server)
    testImplementation(managed.tms.ktorTokenSupport.entraId.verificationMock)
}

application {
    mainClass.set("no.nav.tms.statistikk.ApplicationKt")
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
