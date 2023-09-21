package com.example.mynumbercardidp

import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.spy

@RunWith(MockitoJUnitRunner::class)
class KeycloakConnectionApplicationTest {
    @Test(expected=UninitializedPropertyAccessException::class)
    fun accessContainerBeforeCreate() {
        var keycloakConnectionApplication = KeycloakConnectionApplication()
        keycloakConnectionApplication.container
    }

    @Test
    fun onCreate() {
        var keycloakConnectionApplication = spy(KeycloakConnectionApplication())
        keycloakConnectionApplication.onCreate()
        keycloakConnectionApplication.container
    }
}