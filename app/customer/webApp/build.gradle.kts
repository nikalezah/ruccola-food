plugins {
    id("food.compose.web")
}

food {
    outputName = "customer"
}

foodWeb.configure()

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.app.customer.shared)
            implementation(projects.app.common)
            implementation(projects.core)
        }
    }
}
