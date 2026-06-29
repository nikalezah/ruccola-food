package kz.ruccola.food.gradle

import org.gradle.api.Project
import org.gradle.api.provider.Property
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpackConfig

class FoodWebExtension(
    private val project: Project,
    private val outputName: Property<String>,
) {
    @OptIn(ExperimentalWasmDsl::class)
    fun configure() {
        val name = outputName.get()
        val rootDirPath = project.rootDir.path
        val projectDirPath = project.projectDir.path

        project.kotlin {
            js {
                browser {
                    commonWebpackConfig {
                        outputFileName = "$name.js"
                        devServer = (devServer ?: KotlinWebpackConfig.DevServer()).apply {
                            static(rootDirPath)
                            static(projectDirPath)
                        }
                    }
                }
                binaries.executable()
            }

            wasmJs {
                outputModuleName.set(name)
                browser {
                    commonWebpackConfig {
                        outputFileName = "$name.js"
                        devServer = (devServer ?: KotlinWebpackConfig.DevServer()).apply {
                            static(rootDirPath)
                            static(projectDirPath)
                        }
                    }
                }
                binaries.executable()
            }
        }
    }
}
