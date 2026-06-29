package kz.ruccola.food.gradle

import org.gradle.api.provider.Property

abstract class FoodKmpCoreExtension {
    abstract val namespace: Property<String>
}

abstract class FoodComposeKmpLibraryExtension {
    abstract val namespace: Property<String>
    abstract val resourcesPackage: Property<String>
    abstract val frameworkName: Property<String>
    abstract val appLibrary: Property<Boolean>
}

abstract class FoodAndroidApplicationExtension {
    abstract val applicationId: Property<String>
}

abstract class FoodComposeDesktopExtension {
    abstract val mainClass: Property<String>
}

abstract class FoodComposeWebExtension {
    abstract val outputName: Property<String>
}
