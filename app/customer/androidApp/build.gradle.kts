plugins {
    id("food.android.application")
}

food {
    applicationId = "kz.ruccola.food.customer"
}

android {
    defaultConfig {
        applicationId = food.applicationId.get()
    }
}

dependencies {
    implementation(projects.app.customer.shared)
    implementation(projects.app.common)
    implementation(projects.core)
}
