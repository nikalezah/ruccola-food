package kz.ruccola.food.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.ExtensionAware
import org.jetbrains.compose.ComposeExtension
import org.jetbrains.compose.desktop.DesktopExtension

class FoodComposeDesktopPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        with(project.pluginManager) {
            apply("org.jetbrains.kotlin.multiplatform")
            apply("org.jetbrains.compose")
            apply("org.jetbrains.kotlin.plugin.compose")
        }

        val food = project.extensions.create("food", FoodComposeDesktopExtension::class.java)
        val libs = project.libs

        project.kotlin {
            jvm()
        }

        project.afterEvaluate {
            project.kotlin {
                addSourceSetDependency(project, "jvmMain", project.composeDesktopCurrentOs())
                addSourceSetDependency(project, "jvmMain", libs.library("compose-runtime"))
                addSourceSetDependency(project, "jvmMain", libs.library("compose-ui"))
                addSourceSetDependency(project, "jvmMain", libs.library("androidx-lifecycle-viewmodelCompose"))
                addSourceSetDependency(project, "jvmMain", libs.library("kotlinx-coroutines-swing"))
            }

            val composeExtension = project.extensions.getByType(ComposeExtension::class.java)
            val desktopExtension = (composeExtension as ExtensionAware)
                .extensions
                .getByType(DesktopExtension::class.java)
            desktopExtension.application.mainClass = food.mainClass.get()
        }
    }
}
