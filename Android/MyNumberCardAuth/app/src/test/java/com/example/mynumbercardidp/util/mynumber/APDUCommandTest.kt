package com.example.mynumbercardidp.util.mynumber

import com.example.mynumbercardidp.util.toHexString
import org.junit.Assert.assertEquals
import org.junit.Test

class APDUCommandTest {
    @Test
    fun testGetCommand_case1() {
        var result = APDUCommand.apduCase1(0x00, 0x01, 0x02, 0x03)

        assertEquals(result.command.toHexString(), "00010203")
    }

    @Test
    fun testGetCommand_case2_whenReadBinarySizeIs256BytesOrLess() {
        val result = APDUCommand.apduCase2(0x00, 0xB0.toByte(), 0x00, 0x00, 256)

        assertEquals(result.command.toHexString(), "00B0000000")
    }

    @Test
    fun testGetCommand_case2_whenReadBinarySizeIs257BytesOrOver() {
        val result = APDUCommand.apduCase2(0x00, 0xB0.toByte(), 0x00, 0x00, 257)

        assertEquals(result.command.toHexString(), "00B00000000101")
    }

    @Test
    fun testGetCommand_case3_whenCertificateSizeIs256BytesOrLess() {
        var data =  ByteArray(256) { 1 }
        val result = APDUCommand.apduCase3(0x00, 0x20, 0x00, 0x08, data)
        var expected = byteArrayOf(0x00, 0x20, 0x00, 0x08, 256.toByte()) + data

        assertEquals(result.command.toHexString(), expected.toHexString())
    }

    @Test
    fun testGetCommand_case3_whenCertificateSizeIs257BytesOrOver() {
        var data =  ByteArray(257) { 1 }
        val result = APDUCommand.apduCase3(0x00, 0x20, 0x00, 0x0C, data)
        var expected = byteArrayOf(0x00, 0x20, 0x00, 0x0C, 0, 257.shr(8).toByte(), 257.toByte()) + data

        assertEquals(result.command.toHexString(), expected.toHexString())
    }

    @Test
    fun testGetCommand_case4_whenCertificateSizeIs256BytesOrLess() {
        var data =  ByteArray(256) { 1 }
        val result = APDUCommand.apduCase4(0x80.toByte(), 0x2A, 0x00, 0x00, data, 0)
        var expected = byteArrayOf(0x80.toByte(), 0x2A, 0x00, 0x00, 256.toByte()) + data + 0.toByte()

        assertEquals(result.command.toHexString(), expected.toHexString())
    }

    @Test
    fun testGetCommand_case4_whenLeSizeIs257BytesOrOver() {
        var data =  ByteArray(256) { 1 }
        var le = 257
        var lc = data.size
        val result = APDUCommand.apduCase4(0x80.toByte(), 0x2A, 0x00, 0x00, data, le)
        var expected = byteArrayOf(0x80.toByte(), 0x2A, 0x00, 0x00, 0, lc.shr(8).toByte(), lc.toByte()) +
                                   data + byteArrayOf(le.shr(8).toByte(), le.toByte())

        assertEquals(result.command.toHexString(), expected.toHexString())
    }

    @Test
    fun testGetCommand_case4_whenCertificateSizeIs257BytesOrOver() {
        var data =  ByteArray(257) { 1 }
        var le = 256
        var lc = data.size
        val result = APDUCommand.apduCase4(0x80.toByte(), 0x2A, 0x00, 0x00, data, le)
        var expected = byteArrayOf(0x80.toByte(), 0x2A, 0x00, 0x00, 0, lc.shr(8).toByte(), lc.toByte()) +
                                   data + byteArrayOf(le.shr(8).toByte(), le.toByte())

        assertEquals(result.command.toHexString(), expected.toHexString())
    }
}