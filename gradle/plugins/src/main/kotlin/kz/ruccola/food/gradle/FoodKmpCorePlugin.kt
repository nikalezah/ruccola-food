package kz.ruccola.food.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project

class FoodKmpCorePlugin : Plugin<Project> {
    override fun apply(project: Project) {
        with(project.pluginManager) {
            apply("org.jetbrains.kotlin.multiplatform")
            apply("com.android.kotlin.multiplatform.library")
            apply("org.jetbrains.kotlin.plugin.serialization")
        }

        project.extensions.create("food", FoodKmpCoreExtension::class.java)
        val libs = project.libs

        project.kotlin {
            configureFoodSharedTargets()
        }

        project.afterEvaluate {
            project.kotlin {
                addSourceSetDependency(project, "androidMain", libs.library("ktor-client-android"))
                addSourceSetDependency(project, "commonTest", libs.library("kotlin-test"))
                addSourceSetDependency(project, "jvmMain", libs.library("ktor-client-cio"))
                addSourceSetDependency(project, "iosMain", libs.library("ktor-client-darwin"))
                addSourceSetDependency(project, "webMain", libs.library("ktor-client-core"))
            }
        }
    }
}
