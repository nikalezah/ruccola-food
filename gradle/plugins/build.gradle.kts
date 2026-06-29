plugins {
    `kotlin-dsl`
}

group = "kz.ruccola.food.gradle"

dependencies {
    implementation("com.android.tools.build:gradle:${libs.versions.agp.get()}")
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:${libs.versions.kotlin.get()}")
    implementation("org.jetbrains.compose:compose-gradle-plugin:${libs.versions.compose.multiplatform.get()}")
}

gradlePlugin {
    plugins {
        register("kmpCore") {
            id = "food.kmp.core"
            implementationClass = "kz.ruccola.food.gradle.FoodKmpCorePlugin"
        }
        register("composeKmpLibrary") {
            id = "food.compose.kmp.library"
            implementationClass = "kz.ruccola.food.gradle.FoodComposeKmpLibraryPlugin"
        }
        register("androidApplication") {
            id = "food.android.application"
            implementationClass = "kz.ruccola.food.gradle.FoodAndroidApplicationPlugin"
        }
        register("composeDesktop") {
            id = "food.compose.desktop"
            implementationClass = "kz.ruccola.food.gradle.FoodComposeDesktopPlugin"
        }
        register("composeWeb") {
            id = "food.compose.web"
            implementationClass = "kz.ruccola.food.gradle.FoodComposeWebPlugin"
        }
    }
}
