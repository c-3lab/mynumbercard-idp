package com.example.mynumbercardidp.data

import com.example.mynumbercardidp.network.KeycloakApiService
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

interface AppContainer {
    val keycloakRepository: KeycloakRepository
}

class DefaultAppContainer : AppContainer {
    /* Retrofit.BuilderはbaseUrlが必須のため、初期値としてローカルホストを設定しています。
    POST送信の際、baseUrlは上書きされます。*/
    private val AUTH_SERVER_URL = System.getenv("AUTH_SERVER_URL") ?: "http://127.0.0.1:8080"

    private val client = OkHttpClient.Builder().followRedirects(false).build()

    private val retrofit = Retrofit.Builder()
        .addConverterFactory(GsonConverterFactory.create())
        .client(client)
        .baseUrl(AUTH_SERVER_URL)
        .build()

    private val retrofitService: KeycloakApiService by lazy {
        retrofit.create(KeycloakApiService::class.java)
    }

    override val keycloakRepository: KeycloakRepository by lazy {
        DefaultKeycloakRepository(retrofitService)
    }
}
