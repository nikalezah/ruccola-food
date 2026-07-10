package kz.ruccola.food

import java.io.File
import java.util.Properties

object DesktopPreferences {
    private val prefsFile = File(System.getProperty("user.home"), ".ruccola-food/admin.prefs")
    private val properties = Properties()

    init {
        load()
    }

    fun get(key: String): String? = properties.getProperty(key)

    fun set(key: String, value: String) {
        properties.setProperty(key, value)
        save()
    }

    fun remove(key: String) {
        properties.remove(key)
        save()
    }

    private fun load() {
        if (prefsFile.exists()) {
            prefsFile.inputStream().use { properties.load(it) }
        }
    }

    private fun save() {
        prefsFile.parentFile?.mkdirs()
        prefsFile.outputStream().use { properties.store(it, "Ruccola Food Admin preferences") }
    }
}
