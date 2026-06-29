package kz.ruccola.food.gradle

import org.gradle.api.Project
import org.gradle.api.artifacts.MinimalExternalModuleDependency
import org.gradle.api.provider.Provider
import org.gradle.internal.os.OperatingSystem
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

internal fun KotlinMultiplatformExtension.addSourceSetDependency(
    project: Project,
    sourceSetName: String,
    dependency: Provider<MinimalExternalModuleDependency>,
) {
    project.dependencies.add("${sourceSetName}Implementation", dependency)
}

internal fun KotlinMultiplatformExtension.addSourceSetDependency(
    project: Project,
    sourceSetName: String,
    dependency: String,
) {
    project.dependencies.add("${sourceSetName}Implementation", dependency)
}

internal fun Project.composeDesktopCurrentOs(): String {
    val os = OperatingSystem.current()
    val arch = System.getProperty("os.arch")
    val isArm = arch == "aarch64"
    val target = when {
        os.isWindows -> if (isArm) "windows-arm64" else "windows-x64"
        os.isMacOsX -> if (isArm) "macos-arm64" else "macos-x64"
        else -> if (isArm) "linux-arm64" else "linux-x64"
    }
    val version = libs.findVersion("compose-multiplatform").get().requiredVersion
    return "org.jetbrains.compose.desktop:desktop-jvm-$target:$version"
}
