package kz.ruccola.food

import platform.Foundation.NSUserDefaults

object IosPreferences {
    private val defaults = NSUserDefaults.standardUserDefaults

    fun get(key: String): String? = defaults.stringForKey(key)

    fun set(key: String, value: String) {
        defaults.setObject(value, key)
    }

    fun remove(key: String) {
        defaults.removeObjectForKey(key)
    }
}
