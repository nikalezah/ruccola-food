plugins {
    id("food.android.application")
}

food {
    applicationId = "kz.ruccola.food.admin"
}

android {
    defaultConfig {
        applicationId = food.applicationId.get()
    }
}

dependencies {
    implementation(projects.app.admin.shared)
    implementation(projects.app.common)
    implementation(projects.core)
}
