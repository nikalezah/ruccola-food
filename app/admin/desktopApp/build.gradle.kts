plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

kotlin {
    jvm()

    sourceSets {
        jvmMain.dependencies {
            implementation(projects.app.admin.shared)
            implementation(projects.app.common)
            implementation(projects.core)
            implementation(compose.desktop.currentOs)
            implementation(libs.bundles.desktop.app)
        }
    }
}

compose.desktop { application { mainClass = "kz.ruccola.food.MainKt" } }
