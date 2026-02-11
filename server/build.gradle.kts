plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.ktor)
    application
}

group = "kz.ruccola.food"
version = "1.0.0"
application {
    mainClass.set("kz.ruccola.food.ApplicationKt")

    val isDevelopment: Boolean = project.ext.has("development")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")
}

dependencies {
    implementation(projects.shared)
    implementation(libs.klogging)
    implementation(libs.slf4j.klogging)
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.resources)
    implementation(libs.ktor.server.cio)
    implementation(libs.ktor.server.contentNegotiation)
    implementation(libs.ktor.server.cors)
    implementation(libs.ktor.serialization.json)
    implementation(libs.kotlinx.serialization.json)

    // Database
    implementation(libs.exposed.core)
    implementation(libs.exposed.dao)
    implementation(libs.exposed.r2dbc)
    implementation(libs.exposed.dateTime)
    implementation(libs.r2dbc.pool)
    implementation(libs.r2dbc.postgresql)
    implementation(platform(libs.netty.bom))

    testImplementation(libs.ktor.server.testHost)
    testImplementation(libs.kotlin.test)
}
