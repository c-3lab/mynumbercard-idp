package com.example.mynumbercardidp.network

import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST
import retrofit2.http.Url

interface KeycloakApiService {
    @FormUrlEncoded
    @POST()
    suspend fun authenticate(@Url url: String,
                             @Field("mode") mode: String,
                             @Field("userAuthenticationCertificate") certificate: String,
                             @Field("applicantData") applicantData: String,
                             @Field("sign") sign: String,
    ): Response<Void>

    @FormUrlEncoded
    @POST()
    suspend fun signAuthenticate(@Url url: String,
                                 @Field("mode") mode: String,
                                 @Field("encryptedDigitalSignatureCertificate") certificate: String,
                                 @Field("applicantData") applicantData: String,
                                 @Field("sign") sign: String,
    ): Response<Void>
}
