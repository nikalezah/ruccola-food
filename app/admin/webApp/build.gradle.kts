plugins {
    id("food.compose.web")
}

food {
    outputName = "admin"
}

foodWeb.configure()

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.app.admin.shared)
            implementation(projects.app.common)
            implementation(projects.core)
        }
    }
}
