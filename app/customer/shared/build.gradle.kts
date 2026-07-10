@file:OptIn(ExperimentalKotlinGradlePluginApi::class)

import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidKmpLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

kotlin {
    applyDefaultHierarchyTemplate {
        common {
            group("native") {
                withJvm()
                withIos()
            }
            group("web") {
                withJs()
                withWasmJs()
            }
        }
    }

    android {
        namespace = "kz.ruccola.food.customerlib"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()
        androidResources { enable = true }
    }

    jvm()
    listOf(iosArm64(), iosSimulatorArm64()).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "CustomerShared"
            isStatic = true
        }
    }

    js { browser() }

    @OptIn(ExperimentalWasmDsl::class) wasmJs { browser() }

    sourceSets {
        androidMain.dependencies {
            implementation(libs.bundles.android.compose.library)
            implementation(libs.bundles.ktor.client.android)
        }
        commonMain.dependencies {
            implementation(libs.bundles.compose.ui)
            implementation(libs.bundles.lifecycle.compose)
            implementation(libs.bundles.paging)
            implementation(libs.kotlinx.datetime)
            implementation(libs.compose.uiToolingPreview)
            implementation(projects.app.common)
            implementation(projects.core)
        }
        commonTest.dependencies { implementation(libs.kotlin.test) }
        jvmMain.dependencies {
            implementation(libs.bundles.ktor.client.json)
            implementation(libs.ktor.client.cio)
        }
        iosMain.dependencies {
            implementation(libs.bundles.ktor.client.json)
            implementation(libs.ktor.client.darwin)
        }
        named("webMain") {
            dependencies {
                implementation(libs.bundles.ktor.client.json)
                implementation(libs.kotlinx.browser)
            }
        }
    }
}

dependencies { "androidRuntimeClasspath"(libs.compose.uiTooling) }

compose.resources { packageOfResClass = "food.composeappcustomer.generated.resources" }
