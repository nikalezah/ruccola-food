plugins {
    id("food.compose.desktop")
}

food {
    mainClass = "kz.ruccola.food.MainKt"
}

kotlin {
    sourceSets {
        jvmMain.dependencies {
            implementation(projects.app.admin.shared)
            implementation(projects.app.common)
            implementation(projects.core)
        }
    }
}
