package kz.ruccola.food

import kotlinx.browser.window

actual fun provideAdminToken(): String? = window.localStorage.getItem("admin.token")
