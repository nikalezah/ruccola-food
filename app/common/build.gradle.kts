plugins {
    id("food.compose.kmp.library")
}

food {
    namespace = "kz.ruccola.food.common"
    resourcesPackage = "food.shared.generated.resources"
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
            implementation(projects.core)
        }
    }
}
