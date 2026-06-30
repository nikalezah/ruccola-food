package kz.ruccola.food

import kotlin.test.BeforeTest

abstract class RouteIntegrationTest {
    @BeforeTest
    fun resetDb() {
        resetTestDatabase()
    }
}
