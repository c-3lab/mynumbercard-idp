package com.example.mynumbercardidp.util.mynumber

import android.nfc.Tag
import android.nfc.tech.IsoDep
import android.util.Log
import com.example.mynumbercardidp.util.hexToByteArray
import com.example.mynumbercardidp.util.toHexString

class NfcReader(nfcTag: Tag) {
    companion object {
        private const val logTag = "NfcReader"
    }

    private val isoDep = IsoDep.get(nfcTag)

    fun connect() {
        isoDep.connect()
    }

    fun close() {
        isoDep.close()
    }

    fun isConnected(): Boolean {
        return isoDep.isConnected
    }

    fun selectJpki(): JpkiUtils {
        selectDF("D392F000260100000001".hexToByteArray())
        return JpkiUtils(this)
    }

    fun selectDF(bid: ByteArray) {
        val apduCommand = APDUCommand.apduCase3(0x00, 0xA4.toByte(), 0x04, 0x0C, bid)
        val (sw1, sw2, _) = transceiver(apduCommand)
        if (sw1 != 0x90.toByte() || sw2 != 0x00.toByte()) {
            throw APDUException(sw1, sw2)
        }
    }

    fun selectEF(bid: ByteArray) {
        val apduCommand = APDUCommand.apduCase3(0x00, 0xA4.toByte(), 0x02, 0x0C, bid)
        val (sw1, sw2, _) = transceiver(apduCommand)
        if (sw1 != 0x90.toByte() || sw2 != 0x00.toByte()) {
            throw APDUException(sw1, sw2)
        }
    }

    fun lookupPin(): Int {
        val apduCommand = APDUCommand.apduCase1(0x00, 0x20, 0x00, 0x80.toByte())
        val (sw1, sw2, _) = transceiver(apduCommand)
        return if (sw1 == 0x63.toByte()) {
            sw2.toInt() and 0x0F
        } else {
            -1
        }
    }

    fun verify(pin: String): Boolean {
        if (pin.isEmpty()) {
            return false
        }
        val bpin = pin.toByteArray()
        val apduCommand = APDUCommand.apduCase3(0x00, 0x20, 0x00, 0x80.toByte(), bpin)
        val (sw1, sw2, _) = transceiver(apduCommand)
        if (sw1 == 0x90.toByte() && sw2 == 0x00.toByte()) {
            return true
        } else if (sw1 == 0x63.toByte()) {
            val counter = sw2.toInt() and 0x0F
            if (counter == 0) {
                Log.e(logTag, "Incorrect PIN. blocked.")
                return false
            }
            Log.e(logTag, "Incorrect PIN. You can try $counter more times")
            return false
        } else if (sw1 == 0x69.toByte() && sw2 == 0x84.toByte()) {
            Log.e(logTag, "Your PIN is blocked.")
            return false
        } else {
            Log.e(logTag, "Unknown error.")
            return false
        }
    }

    private fun transceiver(apduCommand: APDUCommand): Triple<Byte, Byte, ByteArray> {
        Log.d(logTag, "Request: ${apduCommand.command.toHexString()}")
        val ret = isoDep.transceive(apduCommand.command)
        Log.d(logTag, "Response: ${ret.toHexString()}")
        if (ret.size >= 2) {
            return Triple(ret[ret.size - 2], ret[ret.size - 1], ret.copyOfRange(0, ret.size - 2))
        }
        return Triple(0, 0, byteArrayOf())
    }

    fun readBinary(size: Int, offset: Int = 0): ByteArray {
        // オフセットの指定値を上位バイトと、下位バイトに変換
        val p1 = (offset shr 8).toByte()
        val p2 = (offset and 0xff).toByte()

        val apduCommand = APDUCommand.apduCase2(0x00, 0xB0.toByte(), p1, p2, size)
        val (_, _, data) = transceiver(apduCommand)
        return data
    }

    fun signature(data: ByteArray): ByteArray {
        val apduCommand = APDUCommand.apduCase4(0x80.toByte(), 0x2A, 0x00, 0x80.toByte(), data, 0)
        val previousTimeout = isoDep.timeout
        isoDep.timeout = 5000
        val (sw1, sw2, res) = transceiver(apduCommand)
        isoDep.timeout = previousTimeout
        if (sw1 == 0x90.toByte() && sw2 == 0x00.toByte()) {
            return res
        }
        return byteArrayOf()
    }
}
