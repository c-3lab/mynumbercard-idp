package com.example.mynumbercardidp.util.mynumber

import org.junit.Assert.assertEquals
import org.junit.Test

class APDUExceptionTest {
    @Test(expected=APDUException::class)
    fun apduExceptionTest01() {
        try {
            throw APDUException(0x90.toByte(), 0x00.toByte())
        } catch (e:APDUException) {
            assertEquals(e.message, "APDU command was not executed normally. The return value are as follows. sw1:-112 sw2:0")

            throw e
        }
    }

    @Test(expected=APDUException::class)
    fun apduExceptionTest02() {
        try {
            throw APDUException(0x05.toByte(), 0xFF.toByte())
        } catch (e:APDUException) {
            assertEquals(e.message, "APDU command was not executed normally. The return value are as follows. sw1:5 sw2:-1")

            throw e
        }
    }

}