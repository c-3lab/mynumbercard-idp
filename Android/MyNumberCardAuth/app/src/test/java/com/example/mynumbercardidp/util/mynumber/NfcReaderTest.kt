package com.example.mynumbercardidp.util.mynumber

import android.nfc.Tag
import android.nfc.tech.IsoDep
import android.util.Log
import com.example.mynumbercardidp.util.hexToByteArray
import com.example.mynumbercardidp.util.toHexString
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.Mockito.doReturn
import org.mockito.Mockito.mock
import org.mockito.Mockito.mockStatic
import org.mockito.MockitoAnnotations
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.any
import org.mockito.kotlin.doNothing
import org.mockito.kotlin.eq
import org.mockito.kotlin.spy
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import java.util.Arrays


@RunWith(MockitoJUnitRunner::class)
class NfcReaderTest {
    private lateinit var closable: AutoCloseable
    private lateinit var mockedIsoDep: IsoDep
    private lateinit var nfcReader: NfcReader

    @Mock
    private lateinit var mockedNfcTag: Tag

    @Before
    fun setUp() {
        closable = MockitoAnnotations.openMocks(this)

        mockedIsoDep = mock(IsoDep::class.java)
        mockStatic(IsoDep::class.java, Mockito.CALLS_REAL_METHODS).use { mocked ->
            mocked.`when`<IsoDep> { IsoDep.get(any()) }.thenReturn(mockedIsoDep)
            nfcReader = spy(NfcReader(mockedNfcTag))
            mocked.verify({ IsoDep.get(eq(mockedNfcTag)) }, times(1))
        }
    }

    @After
    fun tearDown() {
        closable.close()
    }

    @Test
    fun testConnect() {
        nfcReader.connect()

        verify(mockedIsoDep).connect()
    }

    @Test
    fun testClose() {
        nfcReader.close()

        verify(mockedIsoDep).close()
    }

    @Test
    fun testIsConnected() {
        nfcReader.isConnected()

        verify(mockedIsoDep).isConnected
    }
    @Test
    @Suppress("KotlinConstantConditionIf")
    fun testSelectJpki() {
        doNothing().`when`(nfcReader).selectDF(any())

        assertTrue(nfcReader.selectJpki("D392F00026") is JpkiUtils)

        verify(nfcReader).selectDF(eq("D392F00026".hexToByteArray()))
    }

    @Test
    fun testSelectDF_toSucceed() {
        var expectedCommand = "00A4040C0401010101".hexToByteArray()

        var ret: ByteArray = byteArrayOf(0x90.toByte(), 0x00)
        doReturn(ret).`when`(mockedIsoDep).transceive(any())

        mockStatic(Log::class.java, Mockito.CALLS_REAL_METHODS).use { mocked ->
            mocked.`when`<Log> { Log.d(any(), any()) }.then { _ -> null }

            nfcReader.selectDF(byteArrayOf(1, 1, 1, 1))

            verify(mockedIsoDep).transceive(expectedCommand)
            mocked.verify({ Log.d(eq("NfcReader"), eq("Request: ${expectedCommand.toHexString()}")) }, times(1))
            mocked.verify({ Log.d(eq("NfcReader"), eq("Response: ${ret.toHexString()}")) }, times(1))
            mocked.verifyNoMoreInteractions()
        }
    }

    @Test(expected=APDUException::class)
    fun testSelectDF_toFailedWhenInvalidSw1() {
        var expectedCommand = "00A4040C0401010101".hexToByteArray()

        var ret: ByteArray = byteArrayOf(0x90.toByte())
        doReturn(ret).`when`(mockedIsoDep).transceive(any())

        mockStatic(Log::class.java, Mockito.CALLS_REAL_METHODS).use { mocked ->
            mocked.`when`<Log> { Log.d(any(), any()) }.then { _ -> null }

            try {
                nfcReader.selectDF(byteArrayOf(1, 1, 1, 1))
            } catch (e:APDUException) {
                assertEquals(e.message, "APDU command was not executed normally. The return value are as follows. sw1:0 sw2:0")

                verify(mockedIsoDep).transceive(expectedCommand)
                mocked.verify({ Log.d(eq("NfcReader"), eq("Request: ${expectedCommand.toHexString()}")) }, times(1))
                mocked.verify({ Log.d(eq("NfcReader"), eq("Response: ${ret.toHexString()}")) }, times(1))
                mocked.verifyNoMoreInteractions()

                throw e
            }
        }
    }

    @Test(expected=APDUException::class)
    fun testSelectDF_toFailedWhenInvalidSw2() {
        var expectedCommand = "00A4040C0401010101".hexToByteArray()

        var ret: ByteArray = byteArrayOf(0x00, 0x90.toByte(), 0x90.toByte())
        doReturn(ret).`when`(mockedIsoDep).transceive(any())

        mockStatic(Log::class.java, Mockito.CALLS_REAL_METHODS).use { mocked ->
            mocked.`when`<Log> { Log.d(any(), any()) }.then { _ -> null }

            try {
                nfcReader.selectDF(byteArrayOf(1, 1, 1, 1))
            } catch (e:APDUException) {
                assertEquals(e.message, "APDU command was not executed normally. The return value are as follows. sw1:-112 sw2:-112")

                verify(mockedIsoDep).transceive(expectedCommand)
                mocked.verify({ Log.d(eq("NfcReader"), eq("Request: ${expectedCommand.toHexString()}")) }, times(1))
                mocked.verify({ Log.d(eq("NfcReader"), eq("Response: ${ret.toHexString()}")) }, times(1))
                mocked.verifyNoMoreInteractions()

                throw e
            }
        }
    }

    @Test
    fun testSelectEF_toSucceed() {
        var expectedCommand = "00A4020C0401010101".hexToByteArray()

        var ret: ByteArray = byteArrayOf(0x00, 0x90.toByte(), 0x00)
        doReturn(ret).`when`(mockedIsoDep).transceive(any())
        mockStatic(Log::class.java, Mockito.CALLS_REAL_METHODS).use { mocked ->
            mocked.`when`<Log> { Log.d(any(), any()) }.then { _ -> null }

            nfcReader.selectEF(byteArrayOf(1, 1, 1, 1))

            verify(mockedIsoDep).transceive(expectedCommand)
            mocked.verify({ Log.d(eq("NfcReader"), eq("Request: ${expectedCommand.toHexString()}")) }, times(1))
            mocked.verify({ Log.d(eq("NfcReader"), eq("Response: ${ret.toHexString()}")) }, times(1))
            mocked.verifyNoMoreInteractions()
        }
    }

    @Test(expected=APDUException::class)
    fun testSelectEF_toFailedWhenInvalidSw1() {
        var expectedCommand = "00A4020C0401010101".hexToByteArray()

        var ret: ByteArray = byteArrayOf(0x6A.toByte())
        doReturn(ret).`when`(mockedIsoDep).transceive(any())

        mockStatic(Log::class.java, Mockito.CALLS_REAL_METHODS).use { mocked ->
            mocked.`when`<Log> { Log.d(any(), any()) }.then { _ -> null }

            try {
                nfcReader.selectEF(byteArrayOf(1, 1, 1, 1))
            } catch (e:APDUException) {
                assertEquals(e.message, "APDU command was not executed normally. The return value are as follows. sw1:0 sw2:0")

                verify(mockedIsoDep).transceive(expectedCommand)
                mocked.verify({ Log.d(eq("NfcReader"), eq("Request: ${expectedCommand.toHexString()}")) }, times(1))
                mocked.verify({ Log.d(eq("NfcReader"), eq("Response: ${ret.toHexString()}")) }, times(1))
                mocked.verifyNoMoreInteractions()

                throw e
            }
        }
    }

    @Test(expected=APDUException::class)
    fun testSelectEF_toFailedWhenInvalidSw2() {
        var expectedCommand = "00A4020C0401010101".hexToByteArray()

        var ret: ByteArray = byteArrayOf(0x90.toByte(), 0x04)
        doReturn(ret).`when`(mockedIsoDep).transceive(any())

        mockStatic(Log::class.java, Mockito.CALLS_REAL_METHODS).use { mocked ->
            mocked.`when`<Log> { Log.d(any(), any()) }.then { _ -> null }

            try {
                nfcReader.selectEF(byteArrayOf(1, 1, 1, 1))
            } catch (e:APDUException) {
                assertEquals(e.message, "APDU command was not executed normally. The return value are as follows. sw1:-112 sw2:4")

                verify(mockedIsoDep).transceive(expectedCommand)
                mocked.verify({ Log.d(eq("NfcReader"), eq("Request: ${expectedCommand.toHexString()}")) }, times(1))
                mocked.verify({ Log.d(eq("NfcReader"), eq("Response: ${ret.toHexString()}")) }, times(1))
                mocked.verifyNoMoreInteractions()

                throw e
            }
        }
    }

    @Test
    fun testLookupPin_whenNotBlocked() {
        var expectedCommand = "00200080".hexToByteArray()

        var ret: ByteArray = byteArrayOf(0x00, 0x63.toByte(), 0x03)
        doReturn(ret).`when`(mockedIsoDep).transceive(any())

        mockStatic(Log::class.java, Mockito.CALLS_REAL_METHODS).use { mocked ->
            mocked.`when`<Log> { Log.d(any(), any()) }.then { _ -> null }

            assertEquals(nfcReader.lookupPin(), 3)

            verify(mockedIsoDep).transceive(expectedCommand)
            mocked.verify({ Log.d(eq("NfcReader"), eq("Request: ${expectedCommand.toHexString()}")) }, times(1))
            mocked.verify({ Log.d(eq("NfcReader"), eq("Response: ${ret.toHexString()}")) }, times(1))
            mocked.verifyNoMoreInteractions()
        }
    }

    @Test
    fun testLookupPin_whenBlocked() {
        var expectedCommand = "00200080".hexToByteArray()

        var ret: ByteArray = byteArrayOf(0x00, 0x00, 0x00)
        doReturn(ret).`when`(mockedIsoDep).transceive(any())

        mockStatic(Log::class.java, Mockito.CALLS_REAL_METHODS).use { mocked ->
            mocked.`when`<Log> { Log.d(any(), any()) }.then { _ -> null }

            assertEquals(nfcReader.lookupPin(), -1)

            verify(mockedIsoDep).transceive(expectedCommand)
            mocked.verify({ Log.d(eq("NfcReader"), eq("Request: ${expectedCommand.toHexString()}")) }, times(1))
            mocked.verify({ Log.d(eq("NfcReader"), eq("Response: ${ret.toHexString()}")) }, times(1))
            mocked.verifyNoMoreInteractions()
        }
    }

    @Test
    fun testVerify_toSuccessfulExecution() {
        var byteArray: ByteArray = byteArrayOf(1, 1, 1, 1)
        var expectedCommand = "002000800401010101".hexToByteArray()

        var ret: ByteArray = byteArrayOf(0x00, 0x90.toByte(), 0x00)
        doReturn(ret).`when`(mockedIsoDep).transceive(any())

        mockStatic(Log::class.java, Mockito.CALLS_REAL_METHODS).use { mocked ->
            mocked.`when`<Log> { Log.d(any(), any()) }.then { _ -> null }

            assertTrue(nfcReader.verify(String(byteArray)))

            verify(mockedIsoDep).transceive(expectedCommand)
            mocked.verify({ Log.d(eq("NfcReader"), eq("Request: ${expectedCommand.toHexString()}")) }, times(1))
            mocked.verify({ Log.d(eq("NfcReader"), eq("Response: ${ret.toHexString()}")) }, times(1))
            mocked.verifyNoMoreInteractions()
        }
    }

    @Test
    fun testVerify_whenEnterWrongPIN_exceedingLimit() {
        var byteArray: ByteArray = byteArrayOf(1, 1, 1, 1)
        var expectedCommand = "002000800401010101".hexToByteArray()

        var ret: ByteArray = byteArrayOf(0x00, 0x63.toByte(), 0x00)
        doReturn(ret).`when`(mockedIsoDep).transceive(any())

        mockStatic(Log::class.java, Mockito.CALLS_REAL_METHODS).use { mocked ->
            mocked.`when`<Log> { Log.d(any(), any()) }.then { _ -> null }
            mocked.`when`<Log> { Log.e(any(), any()) }.then { _ -> null }

            assertFalse(nfcReader.verify(String(byteArray)))

            verify(mockedIsoDep).transceive(expectedCommand)
            mocked.verify({ Log.d(eq("NfcReader"), eq("Request: ${expectedCommand.toHexString()}")) }, times(1))
            mocked.verify({ Log.d(eq("NfcReader"), eq("Response: ${ret.toHexString()}")) }, times(1))
            mocked.verify({ Log.e(eq("NfcReader"), eq("Incorrect PIN. blocked.")) }, times(1))
            mocked.verifyNoMoreInteractions()
        }
    }

    @Test
    fun testVerify_whenEnterWrongPIN() {
        var byteArray: ByteArray = byteArrayOf(1, 1, 1, 1)
        var expectedCommand = "002000800401010101".hexToByteArray()

        var ret: ByteArray = byteArrayOf(0x00, 0x63.toByte(), 0x01)
        doReturn(ret).`when`(mockedIsoDep).transceive(any())

        mockStatic(Log::class.java, Mockito.CALLS_REAL_METHODS).use { mocked ->
            mocked.`when`<Log> { Log.d(any(), any()) }.then { _ -> null }
            mocked.`when`<Log> { Log.e(any(), any()) }.then { _ -> null }

            assertFalse(nfcReader.verify(String(byteArray)))

            verify(mockedIsoDep).transceive(expectedCommand)
            mocked.verify({ Log.d(eq("NfcReader"), eq("Request: ${expectedCommand.toHexString()}")) }, times(1))
            mocked.verify({ Log.d(eq("NfcReader"), eq("Response: ${ret.toHexString()}")) }, times(1))
            mocked.verify({ Log.e(eq("NfcReader"), eq("Incorrect PIN. You can try 1 more times")) }, times(1))
            mocked.verifyNoMoreInteractions()
        }
    }

    @Test
    fun testVerify_whenEnterPIN_whileBlocked() {
        var byteArray: ByteArray = byteArrayOf(1, 1, 1, 1)
        var expectedCommand = "002000800401010101".hexToByteArray()

        var ret: ByteArray = byteArrayOf(0x00, 0x69.toByte(), 0x84.toByte())
        doReturn(ret).`when`(mockedIsoDep).transceive(any())

        mockStatic(Log::class.java, Mockito.CALLS_REAL_METHODS).use { mocked ->
            mocked.`when`<Log> { Log.d(any(), any()) }.then { _ -> null }
            mocked.`when`<Log> { Log.e(any(), any()) }.then { _ -> null }

            assertFalse(nfcReader.verify(String(byteArray)))

            verify(mockedIsoDep).transceive(expectedCommand)
            mocked.verify({ Log.d(eq("NfcReader"), eq("Request: ${expectedCommand.toHexString()}")) }, times(1))
            mocked.verify({ Log.d(eq("NfcReader"), eq("Response: ${ret.toHexString()}")) }, times(1))
            mocked.verify({ Log.e(eq("NfcReader"), eq("Your PIN is blocked.")) }, times(1))
            mocked.verifyNoMoreInteractions()
        }
    }

    @Test
    fun testVerify_whenReturnUnknownError01() {
        var byteArray: ByteArray = byteArrayOf(1, 1, 1, 1)
        var expectedCommand = "002000800401010101".hexToByteArray()

        var ret: ByteArray = byteArrayOf(0x00, 0x69.toByte(), 0x00)
        doReturn(ret).`when`(mockedIsoDep).transceive(any())

        mockStatic(Log::class.java, Mockito.CALLS_REAL_METHODS).use { mocked ->
            mocked.`when`<Log> { Log.d(any(), any()) }.then { _ -> null }
            mocked.`when`<Log> { Log.e(any(), any()) }.then { _ -> null }

            assertFalse(nfcReader.verify(String(byteArray)))

            verify(mockedIsoDep).transceive(expectedCommand)
            mocked.verify({ Log.d(eq("NfcReader"), eq("Request: ${expectedCommand.toHexString()}")) }, times(1))
            mocked.verify({ Log.d(eq("NfcReader"), eq("Response: ${ret.toHexString()}")) }, times(1))
            mocked.verify({ Log.e(eq("NfcReader"), eq("Unknown error.")) }, times(1))
            mocked.verifyNoMoreInteractions()
        }
    }

    @Test
    fun testVerify_whenReturnUnknownError02() {
        var byteArray: ByteArray = byteArrayOf(1, 1, 1, 1)
        var expectedCommand = "002000800401010101".hexToByteArray()

        var ret: ByteArray = byteArrayOf(0x00, 0x00, 0x84.toByte())
        doReturn(ret).`when`(mockedIsoDep).transceive(any())

        mockStatic(Log::class.java, Mockito.CALLS_REAL_METHODS).use { mocked ->
            mocked.`when`<Log> { Log.d(any(), any()) }.then { _ -> null }
            mocked.`when`<Log> { Log.e(any(), any()) }.then { _ -> null }

            assertFalse(nfcReader.verify(String(byteArray)))

            verify(mockedIsoDep).transceive(expectedCommand)
            mocked.verify({ Log.d(eq("NfcReader"), eq("Request: ${expectedCommand.toHexString()}")) }, times(1))
            mocked.verify({ Log.d(eq("NfcReader"), eq("Response: ${ret.toHexString()}")) }, times(1))
            mocked.verify({ Log.e(eq("NfcReader"), eq("Unknown error.")) }, times(1))
            mocked.verifyNoMoreInteractions()
        }
    }

    @Test
    fun testVerify_whenReturnUnknownError03() {
        var byteArray: ByteArray = byteArrayOf(1, 1, 1, 1)
        var expectedCommand = "002000800401010101".hexToByteArray()

        var ret: ByteArray = byteArrayOf(0x00, 0x90.toByte(), 0x01)
        doReturn(ret).`when`(mockedIsoDep).transceive(any())

        mockStatic(Log::class.java, Mockito.CALLS_REAL_METHODS).use { mocked ->
            mocked.`when`<Log> { Log.d(any(), any()) }.then { _ -> null }
            mocked.`when`<Log> { Log.e(any(), any()) }.then { _ -> null }

            assertFalse(nfcReader.verify(String(byteArray)))

            verify(mockedIsoDep).transceive(expectedCommand)
            mocked.verify({ Log.d(eq("NfcReader"), eq("Request: ${expectedCommand.toHexString()}")) }, times(1))
            mocked.verify({ Log.d(eq("NfcReader"), eq("Response: ${ret.toHexString()}")) }, times(1))
            mocked.verify({ Log.e(eq("NfcReader"), eq("Unknown error.")) }, times(1))
            mocked.verifyNoMoreInteractions()
        }
    }

    @Test
    fun testVerify_whenPinIsEmpty() {
        assertFalse(nfcReader.verify(""))
        verify(mockedIsoDep, times(0)).transceive(any())
    }

    @Test
    fun testReadBinary() {
        var headerSize = 4
        var expectedCommand = "00B0000004".hexToByteArray()

        var ret: ByteArray = byteArrayOf(0x00, 0x00, 0x00)
        doReturn(ret).`when`(mockedIsoDep).transceive(any())

        mockStatic(Log::class.java, Mockito.CALLS_REAL_METHODS).use { mocked ->
            mocked.`when`<Log> { Log.d(any(), any()) }.then { _ -> null }

            assertTrue(Arrays.equals(nfcReader.readBinary(headerSize), byteArrayOf(0x00)))

            verify(mockedIsoDep).transceive(expectedCommand)
            mocked.verify({ Log.d(eq("NfcReader"), eq("Request: ${expectedCommand.toHexString()}")) }, times(1))
            mocked.verify({ Log.d(eq("NfcReader"), eq("Response: ${ret.toHexString()}")) }, times(1))
            mocked.verifyNoMoreInteractions()
        }
    }

    @Test
    fun testReadBinary_whenUsedDefaultArgument() {
        var headerSize = 4
        var expectedCommand = "00B0012C04".hexToByteArray()

        var ret: ByteArray = byteArrayOf(0x00, 0x00, 0x00)
        doReturn(ret).`when`(mockedIsoDep).transceive(any())

        mockStatic(Log::class.java, Mockito.CALLS_REAL_METHODS).use { mocked ->
            mocked.`when`<Log> { Log.d(any(), any()) }.then { _ -> null }

            assertTrue(Arrays.equals(nfcReader.readBinary(headerSize, 300u), byteArrayOf(0x00)))

            verify(mockedIsoDep).transceive(expectedCommand)
            mocked.verify({ Log.d(eq("NfcReader"), eq("Request: ${expectedCommand.toHexString()}")) }, times(1))
            mocked.verify({ Log.d(eq("NfcReader"), eq("Response: ${ret.toHexString()}")) }, times(1))
            mocked.verifyNoMoreInteractions()
        }
    }

    @Test
    fun testComputeSignature_toSucceed() {
        var expectedCommand = "802A012C040101010100".hexToByteArray()

        var ret: ByteArray = byteArrayOf(0x00, 0x90.toByte(), 0x00)
        doReturn(ret).`when`(mockedIsoDep).transceive(any())

        doNothing().`when`(mockedIsoDep).setTimeout(any())
        doReturn(3000).`when`(mockedIsoDep).getTimeout()

        mockStatic(Log::class.java, Mockito.CALLS_REAL_METHODS).use { mocked ->
            mocked.`when`<Log> { Log.d(any(), any()) }.then { _ -> null }

            assertTrue(Arrays.equals(nfcReader.computeSignature(300u, byteArrayOf(1, 1, 1, 1)), byteArrayOf(0x00)))

            verify(mockedIsoDep).transceive(expectedCommand)
            verify(mockedIsoDep).setTimeout(5000)
            verify(mockedIsoDep).setTimeout(3000)
            mocked.verify({ Log.d(eq("NfcReader"), eq("Request: ${expectedCommand.toHexString()}")) }, times(1))
            mocked.verify({ Log.d(eq("NfcReader"), eq("Response: ${ret.toHexString()}")) }, times(1))
            mocked.verifyNoMoreInteractions()
        }
    }

    @Test
    fun testComputeSignature_toReturnByteArray01() {
        var expectedCommand = "802A012C040101010100".hexToByteArray()

        var ret: ByteArray = byteArrayOf(0x00, 0x00, 0x00)
        doReturn(ret).`when`(mockedIsoDep).transceive(any())

        doNothing().`when`(mockedIsoDep).setTimeout(any())
        doReturn(3000).`when`(mockedIsoDep).getTimeout()

        mockStatic(Log::class.java, Mockito.CALLS_REAL_METHODS).use { mocked ->
            mocked.`when`<Log> { Log.d(any(), any()) }.then { _ -> null }

            assertTrue(Arrays.equals(nfcReader.computeSignature(300u, byteArrayOf(1, 1, 1, 1)), byteArrayOf()))

            verify(mockedIsoDep).transceive(expectedCommand)
            verify(mockedIsoDep).setTimeout(5000)
            verify(mockedIsoDep).setTimeout(3000)
            mocked.verify({ Log.d(eq("NfcReader"), eq("Request: ${expectedCommand.toHexString()}")) }, times(1))
            mocked.verify({ Log.d(eq("NfcReader"), eq("Response: ${ret.toHexString()}")) }, times(1))
            mocked.verifyNoMoreInteractions()
        }
    }

    @Test
    fun testComputeSignature_toReturnByteArray02() {
        var expectedCommand = "802A012C040101010100".hexToByteArray()

        var ret: ByteArray = byteArrayOf(0x00, 0x90.toByte(), 0x90.toByte())
        doReturn(ret).`when`(mockedIsoDep).transceive(any())

        doNothing().`when`(mockedIsoDep).setTimeout(any())
        doReturn(3000).`when`(mockedIsoDep).getTimeout()

        mockStatic(Log::class.java, Mockito.CALLS_REAL_METHODS).use { mocked ->
            mocked.`when`<Log> { Log.d(any(), any()) }.then { _ -> null }

            assertTrue(Arrays.equals(nfcReader.computeSignature(300u, byteArrayOf(1, 1, 1, 1)), byteArrayOf()))

            verify(mockedIsoDep).transceive(expectedCommand)
            verify(mockedIsoDep).setTimeout(5000)
            verify(mockedIsoDep).setTimeout(3000)
            mocked.verify({ Log.d(eq("NfcReader"), eq("Request: ${expectedCommand.toHexString()}")) }, times(1))
            mocked.verify({ Log.d(eq("NfcReader"), eq("Response: ${ret.toHexString()}")) }, times(1))
            mocked.verifyNoMoreInteractions()
        }
    }

    @Test
    fun testComputeSignature_toReturnByteArray03() {
        var expectedCommand = "802A012C040101010100".hexToByteArray()

        var ret: ByteArray = byteArrayOf(0x00, 0x00, 0x90.toByte())
        doReturn(ret).`when`(mockedIsoDep).transceive(any())

        doNothing().`when`(mockedIsoDep).setTimeout(any())
        doReturn(3000).`when`(mockedIsoDep).getTimeout()

        mockStatic(Log::class.java, Mockito.CALLS_REAL_METHODS).use { mocked ->
            mocked.`when`<Log> { Log.d(any(), any()) }.then { _ -> null }

            assertTrue(Arrays.equals(nfcReader.computeSignature(300u, byteArrayOf(1, 1, 1, 1)), byteArrayOf()))

            verify(mockedIsoDep).transceive(expectedCommand)
            verify(mockedIsoDep).setTimeout(5000)
            verify(mockedIsoDep).setTimeout(3000)
            mocked.verify({ Log.d(eq("NfcReader"), eq("Request: ${expectedCommand.toHexString()}")) }, times(1))
            mocked.verify({ Log.d(eq("NfcReader"), eq("Response: ${ret.toHexString()}")) }, times(1))
            mocked.verifyNoMoreInteractions()
        }
    }
}