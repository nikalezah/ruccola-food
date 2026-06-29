package kz.ruccola.food.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project

class FoodComposeWebPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        with(project.pluginManager) {
            apply("org.jetbrains.kotlin.multiplatform")
            apply("org.jetbrains.compose")
            apply("org.jetbrains.kotlin.plugin.compose")
        }

        val food = project.extensions.create("food", FoodComposeWebExtension::class.java)
        val libs = project.libs

        project.extensions.add("foodWeb", FoodWebExtension(project, food.outputName))

        project.kotlin {
            configureFoodWebTargets()
        }

        project.afterEvaluate {
            project.kotlin {
                addSourceSetDependency(project, "commonMain", libs.library("compose-runtime"))
                addSourceSetDependency(project, "commonMain", libs.library("compose-ui"))
                addSourceSetDependency(project, "commonMain", libs.library("androidx-lifecycle-viewmodelCompose"))
                addSourceSetDependency(project, "jsMain", libs.library("kotlinx-browser"))
            }
        }
    }
}
