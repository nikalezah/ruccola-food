package kz.ruccola.food.gradle

import com.android.build.api.dsl.ApplicationExtension
import org.gradle.api.Plugin
import org.gradle.api.Project

class FoodAndroidApplicationPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        with(project.pluginManager) {
            apply("com.android.application")
            apply("org.jetbrains.kotlin.plugin.compose")
        }

        project.extensions.create("food", FoodAndroidApplicationExtension::class.java)
        val libs = project.libs

        project.extensions.configure(ApplicationExtension::class.java) {
            namespace = "kz.ruccola.food"
            compileSdk = libs.versionInt("android-compileSdk")

            defaultConfig {
                minSdk = libs.versionInt("android-minSdk")
                targetSdk = libs.versionInt("android-targetSdk")
                versionCode = 1
                versionName = "1.0"
            }
            packaging {
                resources {
                    excludes += "/META-INF/{AL2.0,LGPL2.1}"
                }
            }
            buildTypes {
                getByName("release") {
                    isMinifyEnabled = false
                }
            }
            buildFeatures {
                compose = true
            }
            compileOptions {
                isCoreLibraryDesugaringEnabled = true
            }
        }

        with(project.dependencies) {
            add("implementation", libs.library("androidx-activity-compose"))
            add("implementation", libs.library("compose-runtime"))
            add("implementation", libs.library("compose-foundation"))
            add("implementation", libs.library("compose-ui"))
            add("implementation", libs.library("androidx-lifecycle-viewmodelCompose"))
            add("implementation", libs.library("androidx-lifecycle-runtimeCompose"))
            add("implementation", libs.library("compose-uiToolingPreview"))
            add("debugImplementation", libs.library("compose-uiTooling"))
            add("coreLibraryDesugaring", libs.library("desugar-jdk"))
        }
    }
}
