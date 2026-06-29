package kz.ruccola.food.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.ExtensionAware
import org.jetbrains.compose.ComposeExtension
import org.jetbrains.compose.resources.ResourcesExtension

class FoodComposeKmpLibraryPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        with(project.pluginManager) {
            apply("org.jetbrains.kotlin.multiplatform")
            apply("com.android.kotlin.multiplatform.library")
            apply("org.jetbrains.compose")
            apply("org.jetbrains.kotlin.plugin.compose")
        }

        val food = project.extensions.create("food", FoodComposeKmpLibraryExtension::class.java).apply {
            appLibrary.convention(false)
        }
        val libs = project.libs

        project.kotlin {
            configureFoodSharedTargets()
        }

        project.afterEvaluate {
            val isAppLibrary = food.appLibrary.get()

            project.kotlin {
                configureFoodIosFramework(food.frameworkName.orNull)

                addSourceSetDependency(project, "androidMain", libs.library("androidx-activity-compose"))
                addSourceSetDependency(project, "androidMain", libs.library("coil-compose"))
                addSourceSetDependency(project, "commonMain", libs.library("compose-runtime"))
                addSourceSetDependency(project, "commonMain", libs.library("compose-foundation"))
                addSourceSetDependency(project, "commonMain", libs.library("compose-animation"))
                addSourceSetDependency(project, "commonMain", libs.library("compose-material3"))
                addSourceSetDependency(project, "commonMain", libs.library("compose-ui"))
                addSourceSetDependency(project, "commonMain", libs.library("compose-components-resources"))
                addSourceSetDependency(project, "commonMain", libs.library("kotlinx-datetime"))
                addSourceSetDependency(project, "commonMain", libs.library("androidx-lifecycle-viewmodelCompose"))
                addSourceSetDependency(project, "commonMain", libs.library("androidx-lifecycle-runtimeCompose"))
                addSourceSetDependency(project, "commonTest", libs.library("kotlin-test"))
                addSourceSetDependency(project, "jvmMain", libs.library("ktor-client-core"))
                addSourceSetDependency(project, "jvmMain", libs.library("ktor-client-cio"))
                addSourceSetDependency(project, "iosMain", libs.library("ktor-client-core"))
                addSourceSetDependency(project, "iosMain", libs.library("ktor-client-darwin"))
                addSourceSetDependency(project, "webMain", libs.library("ktor-client-core"))

                if (isAppLibrary) {
                    listOf(
                        "compose-uiToolingPreview",
                        "androidx-appcompat",
                        "androidx-core-ktx",
                        "androidx-datastore-preferences",
                        "ktor-client-core",
                        "ktor-client-contentNegotiation",
                        "ktor-client-android",
                        "ktor-serialization-json",
                    ).forEach { alias ->
                        addSourceSetDependency(project, "androidMain", libs.library(alias))
                    }
                    listOf(
                        "compose-uiToolingPreview",
                        "androidx-paging-compose",
                        "androidx-paging-common",
                    ).forEach { alias ->
                        addSourceSetDependency(project, "commonMain", libs.library(alias))
                    }
                    listOf(
                        "ktor-client-contentNegotiation",
                        "ktor-serialization-json",
                    ).forEach { alias ->
                        addSourceSetDependency(project, "jvmMain", libs.library(alias))
                        addSourceSetDependency(project, "iosMain", libs.library(alias))
                        addSourceSetDependency(project, "webMain", libs.library(alias))
                    }
                    addSourceSetDependency(project, "webMain", libs.library("kotlinx-browser"))
                } else {
                    addSourceSetDependency(project, "nativeMain", libs.library("ktor-client-core"))
                }
            }

            if (food.appLibrary.get()) {
                project.dependencies.add("androidRuntimeClasspath", libs.library("compose-uiTooling"))
            }

            val composeExtension = project.extensions.getByType(ComposeExtension::class.java)
            (composeExtension as ExtensionAware).extensions.configure(ResourcesExtension::class.java) {
                packageOfResClass = food.resourcesPackage.get()
            }
        }
    }
}
