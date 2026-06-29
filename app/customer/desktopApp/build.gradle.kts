plugins {
    id("food.compose.desktop")
}

food {
    mainClass = "kz.ruccola.food.customer.MainKt"
}

kotlin {
    sourceSets {
        jvmMain.dependencies {
            implementation(projects.app.customer.shared)
            implementation(projects.app.common)
            implementation(projects.core)
        }
    }
}
