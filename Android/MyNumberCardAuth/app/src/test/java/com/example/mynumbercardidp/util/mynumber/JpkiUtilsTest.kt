package com.example.mynumbercardidp.util.mynumber

import android.nfc.Tag
import android.nfc.tech.IsoDep
import android.util.Log
import com.example.mynumbercardidp.util.hexToByteArray
import com.example.mynumbercardidp.util.toHexString
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.Mockito.mock
import org.mockito.Mockito.mockStatic
import org.mockito.MockitoAnnotations
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.any
import org.mockito.kotlin.doNothing
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.spy
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import java.util.Arrays

@RunWith(MockitoJUnitRunner::class)
class JpkiUtilsTest {
    private lateinit var closable: AutoCloseable
    private lateinit var jpkiUtils: JpkiUtils

    @Mock
    private lateinit var mockedNfcReader: NfcReader

    @Before
    fun setUp() {
        closable = MockitoAnnotations.openMocks(this)

        jpkiUtils = JpkiUtils(mockedNfcReader)
    }

    @After
    fun tearDown() {
        closable.close()
    }

    @Test
    fun testLookupPin() {
        doReturn(0).`when`(mockedNfcReader).lookupPin()
        assertEquals(0, jpkiUtils.lookupPin("00010203"))

        verify(mockedNfcReader).selectEF("00010203".hexToByteArray())
        verify(mockedNfcReader).lookupPin()
    }

    @Test
    fun testVerifyPin() {
        doReturn(true).`when`(mockedNfcReader).verify(any())
        assertTrue(jpkiUtils.verifyPin("00010203", "1234"))

        verify(mockedNfcReader).selectEF("00010203".hexToByteArray())
        verify(mockedNfcReader).verify("1234")
    }

    @Test
    fun testReadCertificate() {
        // UShortを引数に使うメソッドがモック化できないため、nfcReaderを作成
        var mockedNfcTag: Tag = mock(Tag::class.java)
        var mockedIsoDep: IsoDep = mock(IsoDep::class.java)

        mockStatic(IsoDep::class.java, Mockito.CALLS_REAL_METHODS).use { mocked ->
            mocked.`when`<IsoDep> { IsoDep.get(any()) }.thenReturn(mockedIsoDep)
            mockedNfcReader = spy(NfcReader(mockedNfcTag))
            mocked.verify({ IsoDep.get(eq(mockedNfcTag)) }, times(1))
        }

        doNothing().`when`(mockedNfcReader).selectEF(any())

        var headerSize = 4
        var ret: ByteArray = byteArrayOf(0x00, 0x90.toByte(), 0x00, 0x01, 0x02, 0x03)
        doReturn(ret).`when`(mockedIsoDep).transceive(any())

        jpkiUtils = JpkiUtils(mockedNfcReader)
        mockStatic(Log::class.java, Mockito.CALLS_REAL_METHODS).use { mocked ->
            mocked.`when`<Log> { Log.d(any(), any()) }.then { _ -> null }

            assertEquals(jpkiUtils.readCertificate("01010101").toHexString(), "0090000100900001")
        }
        verify(mockedNfcReader).selectEF("01010101".hexToByteArray())
        verify(mockedNfcReader).readBinary(headerSize)
    }

    @Test
    fun testReadCertificate_whenCurrentDataUsedInReadBinaryIsEmpty() {
        // UShortを引数に使うメソッドがモック化できないため、nfcReaderを作成
        var mockedNfcTag: Tag = mock(Tag::class.java)
        var mockedIsoDep: IsoDep = mock(IsoDep::class.java)

        mockStatic(IsoDep::class.java, Mockito.CALLS_REAL_METHODS).use { mocked ->
            mocked.`when`<IsoDep> { IsoDep.get(any()) }.thenReturn(mockedIsoDep)
            mockedNfcReader = spy(NfcReader(mockedNfcTag))
            mocked.verify({ IsoDep.get(eq(mockedNfcTag)) }, times(1))
        }

        doNothing().`when`(mockedNfcReader).selectEF(any())

        var headerSize = 4
        var ret: ByteArray = byteArrayOf(0x00, 0x90.toByte(), 0x00, 0x01, 0x02, 0x03)
        doReturn(ret).doReturn(byteArrayOf()).`when`(mockedIsoDep).transceive(any())

        jpkiUtils = JpkiUtils(mockedNfcReader)
        mockStatic(Log::class.java, Mockito.CALLS_REAL_METHODS).use { mocked ->
            mocked.`when`<Log> { Log.d(any(), any()) }.then { _ -> null }

            assertEquals(jpkiUtils.readCertificate("01010101").toHexString(), "")
        }
        verify(mockedNfcReader).selectEF("01010101".hexToByteArray())
        verify(mockedNfcReader).readBinary(headerSize)
    }

    @Test
    fun testReadCertificate_whenHeaderIsEmpty() {
        // UShortを引数に使うメソッドがモック化できないため、nfcReaderを作成
        var mockedNfcTag: Tag = mock(Tag::class.java)
        var mockedIsoDep: IsoDep = mock(IsoDep::class.java)

        mockStatic(IsoDep::class.java, Mockito.CALLS_REAL_METHODS).use { mocked ->
            mocked.`when`<IsoDep> { IsoDep.get(any()) }.thenReturn(mockedIsoDep)
            mockedNfcReader = spy(NfcReader(mockedNfcTag))
            mocked.verify({ IsoDep.get(eq(mockedNfcTag)) }, times(1))
        }

        doNothing().`when`(mockedNfcReader).selectEF(any())

        var headerSize = 4
        var readBinaryCommand: ByteArray = byteArrayOf(0x00, 0xB0.toByte(), 0x00, 0x00, headerSize.toByte())

        var ret: ByteArray = byteArrayOf(0x00)
        doReturn(ret).`when`(mockedIsoDep).transceive(any())

        jpkiUtils = JpkiUtils(mockedNfcReader)
        mockStatic(Log::class.java, Mockito.CALLS_REAL_METHODS).use { mocked ->
            mocked.`when`<Log> { Log.d(any(), any()) }.then { _ -> null }

            assertTrue(Arrays.equals(jpkiUtils.readCertificate("01010101"), byteArrayOf()))

            mocked.verify({ Log.d(eq("NfcReader"), eq("Request: ${readBinaryCommand.toHexString()}")) }, times(1))
            mocked.verify({ Log.d(eq("NfcReader"), eq("Response: ${ret.toHexString()}")) }, times(1))
        }
        verify(mockedNfcReader).selectEF("01010101".hexToByteArray())
        verify(mockedNfcReader).readBinary(headerSize)
    }

    @Test
    fun testComputeSignature() {
        // UShortを引数に使うメソッドがモック化できないため、nfcReaderを作成
        var mockedNfcTag: Tag = mock(Tag::class.java)
        var mockedIsoDep: IsoDep = mock(IsoDep::class.java)

        mockStatic(IsoDep::class.java, Mockito.CALLS_REAL_METHODS).use { mocked ->
            mocked.`when`<IsoDep> { IsoDep.get(any()) }.thenReturn(mockedIsoDep)
            mockedNfcReader = spy(NfcReader(mockedNfcTag))
            mocked.verify({ IsoDep.get(eq(mockedNfcTag)) }, times(1))
        }

        var byteArray: ByteArray = byteArrayOf(1, 1, 1, 1)
        var selectEFCommand: ByteArray = byteArrayOf(0x00, 0xA4.toByte(), 0x02, 0x0C, byteArray.size.toByte()) + byteArray
        var signatureCommand: ByteArray = byteArrayOf(0x80.toByte(), 0x2A, 0x01, 0x2C, byteArray.size.toByte()) + byteArray + 0.toByte()

        var ret: ByteArray = byteArrayOf(0x00, 0x90.toByte(), 0x00)
        doReturn(ret).`when`(mockedIsoDep).transceive(any())

        doNothing().`when`(mockedIsoDep).setTimeout(any())
        doReturn(3000).`when`(mockedIsoDep).getTimeout()

        jpkiUtils = JpkiUtils(mockedNfcReader)
        mockStatic(Log::class.java, Mockito.CALLS_REAL_METHODS).use { mocked ->
            mocked.`when`<Log> { Log.d(any(), any()) }.then { _ -> null }

            assertTrue(Arrays.equals(jpkiUtils.computeSignature("01010101", "12C", byteArray), byteArrayOf(0x00)))

            // selectEF
            mocked.verify({ Log.d(eq("NfcReader"), eq("Request: ${selectEFCommand.toHexString()}")) }, times(1))

            // computeSignature
            mocked.verify({ Log.d(eq("NfcReader"), eq("Request: ${signatureCommand.toHexString()}")) }, times(1))
            mocked.verify({ Log.d(eq("NfcReader"), eq("Response: ${ret.toHexString()}")) }, times(2))

            mocked.verifyNoMoreInteractions()
        }
        verify(mockedNfcReader).selectEF(eq("01010101".hexToByteArray()))
        verify(mockedIsoDep).transceive(eq(selectEFCommand))
        verify(mockedIsoDep).transceive(eq(signatureCommand))
        verify(mockedIsoDep).setTimeout(eq(5000))
        verify(mockedIsoDep).setTimeout(eq(3000))
    }
 }