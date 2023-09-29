package com.example.mynumbercardidp.data

import com.example.mynumbercardidp.network.KeycloakApiService
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.anyString
import org.mockito.Mockito.mock
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.doReturn
import retrofit2.Response


class KeycloakRepositoryTest {
    private lateinit var closable: AutoCloseable

    @Mock
    private lateinit var keycloakApiService: KeycloakApiService

    @Before
    fun setUp() {
        closable = MockitoAnnotations.openMocks(this)

        keycloakApiService = mock(KeycloakApiService::class.java)
    }

    @After
    fun tearDown() {
        closable.close()
    }

    @Test
    fun jpkiAuthenticate() {
        val url = "https://example.com/authenticate"
        val mode = "login"
        val certificate = "Dummy certificate"
        val applicantData = "Dummy applicantData"
        val sign = "Dummy sign"

        var expectedResponse = mock(Response::class.java)
        var actualResponse = runBlocking {
            doReturn(expectedResponse).`when`(keycloakApiService).authenticate(anyString(), anyString(), anyString(), anyString(), anyString())

            var repository = DefaultKeycloakRepository(keycloakApiService)
            var result = repository.jpkiAuthenticate(url, mode, certificate, applicantData, sign)

            verify(keycloakApiService, times(1)).authenticate(url, mode, certificate, applicantData, sign)

            return@runBlocking result
        }
        assertEquals(expectedResponse, actualResponse)
    }

    @Test
    fun jpkiSignAuthenticate() {
        val url = "https://example.com/authenticate"
        val mode = "login"
        val certificate = "Dummy certificate"
        val applicantData = "Dummy applicantData"
        val sign = "Dummy sign"

        var expectedResponse = mock(Response::class.java)
        var actualResponse = runBlocking {
            doReturn(expectedResponse).`when`(keycloakApiService).signAuthenticate(anyString(), anyString(), anyString(), anyString(), anyString())

            var repository = DefaultKeycloakRepository(keycloakApiService)
            var result = repository.jpkiSignAuthenticate(url, mode, certificate, applicantData, sign)

            verify(keycloakApiService, times(1)).signAuthenticate(url, mode, certificate, applicantData, sign)

            return@runBlocking result
        }
        assertEquals(expectedResponse, actualResponse)
    }
}