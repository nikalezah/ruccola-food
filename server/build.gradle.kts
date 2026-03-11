plugins {
    // todo: move to a root `build.gradle.kts` for other modules when works with all KMP targets
    alias(libs.plugins.dependencyAnalysis)

    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.ktor)
    alias(libs.plugins.serialization)
    application
}

group = "kz.ruccola.food"
version = "1.0.0"
application {
    mainClass.set("kz.ruccola.food.ApplicationKt")

    val isDevelopment: Boolean = project.ext.has("development")
    applicationDefaultJvmArgs = listOf(
        "-Dio.ktor.development=$isDevelopment",
        // Required for Java 21+ to allow libraries (like Jansi for logging colors or Netty for networking)
        // to call native OS APIs without security warnings.
        "--enable-native-access=ALL-UNNAMED",
    )
}

dependencies {
    implementation(projects.shared)
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.resources)
    implementation(libs.ktor.server.cio)
    implementation(libs.ktor.server.call.logging)
    implementation(libs.ktor.server.contentNegotiation)
    implementation(libs.ktor.server.cors)
    implementation(libs.ktor.server.auth)
    implementation(libs.ktor.server.auth.jwt)
    implementation(libs.ktor.serialization.json)
    implementation(libs.kotlinx.serialization.json)
    runtimeOnly(libs.slf4j.klogging)

    // Database
    implementation(libs.exposed.core)
    implementation(libs.exposed.r2dbc)
    implementation(libs.exposed.dateTime)
    runtimeOnly(libs.exposed.dao)
    runtimeOnly(libs.r2dbc.pool)
    runtimeOnly(libs.r2dbc.postgresql)
    implementation(platform(libs.netty.bom))

    testImplementation(libs.ktor.server.testHost)
    testImplementation(libs.ktor.client.contentNegotiation)
    testImplementation(libs.kotlin.test)
}
