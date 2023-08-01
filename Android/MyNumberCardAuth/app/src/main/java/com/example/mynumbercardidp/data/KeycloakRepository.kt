package com.example.mynumbercardidp.data

import com.example.mynumbercardidp.network.KeycloakApiService
import retrofit2.Response

interface KeycloakRepository {
    suspend fun jpkiAuthenticate(
        url: String,
        mode: String,
        certificate: String,
        applicantData: String,
        sign: String,
    ):Response<Void>

    suspend fun jpkiSignAuthenticate(
        url: String,
        mode: String,
        certificate: String,
        applicantData: String,
        sign: String,
    ):Response<Void>
}

class DefaultKeycloakRepository(
    private val keycloakApiService: KeycloakApiService
) : KeycloakRepository {
    override suspend fun jpkiAuthenticate(
        url: String,
        mode: String,
        certificate: String,
        applicantData: String,
        sign: String,
    ): Response<Void> {
        return keycloakApiService.authenticate(
            url,
            mode,
            certificate,
            applicantData,
            sign,
        )
    }

    override suspend fun jpkiSignAuthenticate(
        url: String,
        mode: String,
        certificate: String,
        applicantData: String,
        sign: String,
    ): Response<Void> {
        return keycloakApiService.signAuthenticate(
            url,
            mode,
            certificate,
            applicantData,
            sign,
        )
    }
}
