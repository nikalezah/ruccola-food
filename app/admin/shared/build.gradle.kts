plugins {
    id("food.compose.kmp.library")
}

food {
    namespace = "kz.ruccola.food.adminlib"
    frameworkName = "AdminShared"
    resourcesPackage = "food.composeappadmin.generated.resources"
    appLibrary = true
}

kotlin {
    android {
        namespace = food.namespace.get()
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()
        androidResources {
            enable = true
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(projects.app.common)
            implementation(projects.core)
        }
    }
}
