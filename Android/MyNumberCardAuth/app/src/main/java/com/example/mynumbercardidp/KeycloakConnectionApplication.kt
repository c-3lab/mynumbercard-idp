package com.example.mynumbercardidp

import android.app.Application
import com.example.mynumbercardidp.data.AppContainer
import com.example.mynumbercardidp.data.DefaultAppContainer

class KeycloakConnectionApplication : Application() {
    lateinit var container: AppContainer
    override fun onCreate() {
        super.onCreate()
        container = DefaultAppContainer()
    }
}
