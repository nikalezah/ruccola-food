import com.android.build.api.dsl.ApplicationExtension
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jlleitschuh.gradle.ktlint.KtlintExtension

fun jvmTargetOf(version: String): JvmTarget = JvmTarget.valueOf("JVM_$version")

plugins {
    // this is necessary to avoid the plugins to be loaded multiple times
    // in each subproject's classloader
    alias(libs.plugins.dependencyAnalysis)
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.androidKmpLibrary) apply false
    alias(libs.plugins.composeMultiplatform) apply false
    alias(libs.plugins.composeCompiler) apply false
    alias(libs.plugins.kotlinJvm) apply false
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.ktor) apply false
    alias(libs.plugins.ktlint) apply false
}

subprojects {
    // todo: uncomment when works with all KMP targets
    // pluginManager.apply(rootProject.libs.plugins.dependencyAnalysis.get().pluginId)
    pluginManager.apply(rootProject.libs.plugins.ktlint.get().pluginId)

    configure<KtlintExtension> {
        version.set(rootProject.libs.versions.ktlint.engine)
        ignoreFailures.set(true)
        filter {
            exclude { it.file.path.contains("${File.separator}build${File.separator}") }
        }
    }

    tasks.withType<KotlinCompile>().configureEach {
        dependsOn("ktlintFormat")
    }

    afterEvaluate {
        val androidJava = rootProject.libs.versions.java.android.get()
        val jvmJava = rootProject.libs.versions.java.jvm.get()

        extensions.findByType<ApplicationExtension>()?.compileOptions {
            sourceCompatibility = JavaVersion.toVersion(androidJava)
            targetCompatibility = JavaVersion.toVersion(androidJava)
        }

        extensions.findByType<KotlinJvmProjectExtension>()?.apply {
            jvmToolchain(jvmJava.toInt())
            compilerOptions {
                jvmTarget.set(jvmTargetOf(jvmJava))
            }
        }

        tasks.withType<KotlinCompile>().configureEach {
            compilerOptions {
                val isAndroid =
                    name.contains("Android", ignoreCase = true) ||
                        pluginManager.hasPlugin("com.android.application")
                jvmTarget.set(if (isAndroid) jvmTargetOf(androidJava) else jvmTargetOf(jvmJava))
            }
        }
    }
}
