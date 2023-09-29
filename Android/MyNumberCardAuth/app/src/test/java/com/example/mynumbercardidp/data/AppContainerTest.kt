package com.example.mynumbercardidp.data

import org.junit.Assert.assertEquals
import org.junit.Ignore
import org.junit.Test
import java.lang.reflect.Field

class AppContainerTest {
    @Ignore
    @Test
    fun getAUTH_SERVER_URL() {
        this.changeEnvironmentVariables("AUTH_SERVER_URL", "https://example.com")

        var defaultAppContainer = DefaultAppContainer()

        var authServerUrl = DefaultAppContainer::class.java.getDeclaredField("AUTH_SERVER_URL")
        authServerUrl.isAccessible = true
        assertEquals(authServerUrl.get(defaultAppContainer), "https://example.com")
    }

    @Ignore
    @Test
    fun getLocalHost() {
        this.changeEnvironmentVariables("AUTH_SERVER_URL", null)

        var defaultAppContainer = DefaultAppContainer()

        var authServerUrl = DefaultAppContainer::class.java.getDeclaredField("AUTH_SERVER_URL")
        authServerUrl.isAccessible = true
        assertEquals(authServerUrl.get(defaultAppContainer), "http://127.0.0.1:8080")
    }

    private fun changeEnvironmentVariables(key: String, value: String?) {
        var clazz: Class<*>  = Class.forName("java.lang.ProcessEnvironment")
        var theCaseInsensitiveEnvironment: Field = clazz.getDeclaredField("theCaseInsensitiveEnvironment")
        theCaseInsensitiveEnvironment.isAccessible = true

        var systemEnviroment: MutableMap<String,String> = theCaseInsensitiveEnvironment.get(null) as MutableMap<String, String>
        if (value == null) {
            systemEnviroment.remove(key)
        } else {
            systemEnviroment.put(key, value)
        }
    }
}