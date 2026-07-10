plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeCompiler)
}

android {
    namespace = "kz.ruccola.food"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "kz.ruccola.food.admin"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0"
    }
    packaging { resources { excludes += "/META-INF/{AL2.0,LGPL2.1}" } }
    buildTypes { getByName("release") { isMinifyEnabled = false } }
    buildFeatures { compose = true }
    compileOptions { isCoreLibraryDesugaringEnabled = true }
}

dependencies {
    implementation(projects.app.admin.shared)
    implementation(projects.app.common)
    implementation(projects.core)
    implementation(libs.bundles.android.compose.app)
    debugImplementation(libs.compose.uiTooling)
    coreLibraryDesugaring(libs.desugar.jdk)
}
