import org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jlleitschuh.gradle.ktlint.KtlintExtension

plugins {
    // this is necessary to avoid the plugins to be loaded multiple times
    // in each subproject's classloader
    alias(libs.plugins.dependencyAnalysis)
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.androidLibrary) apply false
    alias(libs.plugins.composeMultiplatform) apply false
    alias(libs.plugins.composeCompiler) apply false
    alias(libs.plugins.kotlinJvm) apply false
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.ktor) apply false
    alias(libs.plugins.ktlint) apply false
}

subprojects {
    // todo: uncomment when works with all KMP targets
    // apply(plugin = rootProject.libs.plugins.dependencyAnalysis.get().pluginId)
    apply(plugin = rootProject.libs.plugins.ktlint.get().pluginId)

    configure<KtlintExtension> {
        version.set(rootProject.libs.versions.ktlint.engine)
        ignoreFailures.set(true)
        filter {
            exclude { it.file.path.contains("${File.separator}build${File.separator}") }
        }
    }

    // todo: remove when explicit-backing-fields becomes a standard feature
    tasks.withType<KotlinCompilationTask<*>>().configureEach {
        compilerOptions {
            freeCompilerArgs.addAll(
                "-Xexplicit-backing-fields",
                // "-Xreturn-value-checker=full",
                // "-XXLanguage:+NameBasedDestructuring",
            )
        }
    }
    tasks.withType<KotlinCompile>().configureEach {
        dependsOn("ktlintFormat")
    }
}
