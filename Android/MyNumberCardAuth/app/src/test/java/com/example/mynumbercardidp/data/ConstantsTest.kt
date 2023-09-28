package com.example.mynumbercardidp.data

import org.junit.Assert.assertEquals
import org.junit.Test

class Rfc3447HashPrefixTest {
    @Test
    fun valueTest_forMD2() {
        val hashPrefix = Rfc3447HashPrefix.MD2.toString()
        assertEquals("3020300c06082a864886f70d020205000410", hashPrefix)
    }

    @Test
    fun valueTest_forMD5() {
        val hashPrefix = Rfc3447HashPrefix.MD5.toString()
        assertEquals("3020300c06082a864886f70d020505000410", hashPrefix)
    }

    @Test
    fun valueTest_forSH1() {
        val hashPrefix = Rfc3447HashPrefix.SHA1.toString()
        assertEquals("3021300906052b0e03021a05000414", hashPrefix)
    }

    @Test
    fun valueTest_forSHA256() {
        val hashPrefix = Rfc3447HashPrefix.SHA256.toString()
        assertEquals("3031300d060960864801650304020105000420", hashPrefix)
    }

    @Test
    fun valueTest_forSHA384() {
        val hashPrefix = Rfc3447HashPrefix.SHA384.toString()
        assertEquals("3041300d060960864801650304020205000430", hashPrefix)
    }

    @Test
    fun valueTest_forSHA512() {
        val hashPrefix = Rfc3447HashPrefix.SHA512.toString()
        assertEquals("3051300d060960864801650304020305000440", hashPrefix)
    }
}

class ValidInputTextTest {
    @Test
    fun lengthTest_forCertForUserVerification() {
        assertEquals(4, ValidInputText.CertForUserVerification.length)
    }

    @Test
    fun lengthTest_forCertForSign() {
        assertEquals(6, ValidInputText.CertForSign.length)
    }
}

class MaxInputTextTest {
    @Test
    fun lengthTest_forCertForUserVerification() {
        assertEquals(4, MaxInputText.CertForUserVerification.length)
    }

    @Test
    fun lengthTest_forCertForSign() {
        assertEquals(16, MaxInputText.CertForSign.length)
    }
}

class HttPStatusCodeTest {
    @Test
    fun valueTest_forFound() {
        assertEquals(302, HttPStatusCode.Found.value)
    }

    @Test
    fun valueTest_forBadRequest() {
        assertEquals(400, HttPStatusCode.BadRequest.value)
    }

    @Test
    fun valueTest_forUnauthorized() {
        assertEquals(401, HttPStatusCode.Unauthorized.value)
    }

    @Test
    fun valueTest_forNotFound() {
        assertEquals(404, HttPStatusCode.NotFound.value)
    }

    @Test
    fun valueTest_forConflict() {
        assertEquals(409, HttPStatusCode.Conflict.value)
    }

    @Test
    fun valueTest_forGone() {
        assertEquals(410, HttPStatusCode.Gone.value)
    }

    @Test
    fun valueTest_forInternalServerError() {
        assertEquals(500, HttPStatusCode.InternalServerError.value)
    }

    @Test
    fun valueTest_forServiceUnavailable() {
        assertEquals(503, HttPStatusCode.ServiceUnavailable.value)
    }
}