package com.example.mynumbercardidp.util.mynumber

import com.example.mynumbercardidp.util.hexToByteArray
import java.nio.ByteBuffer

class JpkiUtils(private val reader: NfcReader) {
    companion object {
        private const val logTag = "JpkiUtils"
        private const val headerSize = 4 //データサイズなどの情報部が格納されている先頭部のバイト数
    }

    fun lookupAuthPin(): Int {
        reader.selectEF("0018".hexToByteArray())
        return reader.lookupPin()
    }

    fun verifyAuthPin(pin: String): Boolean {
        reader.selectEF("0018".hexToByteArray())
        return reader.verify(pin)
    }

    fun lookupSignPin(): Int {
        reader.selectEF("001B".hexToByteArray())
        return reader.lookupPin()
    }

    fun verifySignPin(pin: String): Boolean {
        reader.selectEF("001B".hexToByteArray())
        return reader.verify(pin)
    }

    fun readCertificateUserVerification(): ByteArray {
        return readCertificate("000A".hexToByteArray())
    }

    fun readCertificateUserVerificationCA(): ByteArray {
        return readCertificate("000B".hexToByteArray())
    }

    fun readCertificateSign(): ByteArray {
        return readCertificate("0001".hexToByteArray())
    }

    fun readCertificateSignCA(): ByteArray {
        return readCertificate("0001".hexToByteArray())
    }

    private fun readCertificate(efid: ByteArray): ByteArray {
        reader.selectEF(efid)

        // 読み込むべきサイズを取得するため、先頭4バイト取得
        val header = reader.readBinary(headerSize)
        if (header.isEmpty()) { return byteArrayOf() }

        // 読み込んだデータから、データ全体量の格納部分を抽出
        val sizeToRead = ByteBuffer.wrap(header, 2, 2).short

        // 全体を読み込み、データを返却する
        return readBinary(sizeToRead + headerSize)
    }

    private fun readBinary(expectedSize: Int): ByteArray {
        var data = byteArrayOf()

        // 不足サイズ分が取り切れるまでREAD BINARYを繰り返す
        while (data.size < expectedSize) {
            var currentData = reader.readBinary(expectedSize - data.size, data.size)
            if (currentData.isEmpty()) { break }
            data += currentData
        }

        return data
    }

    fun authSignature(nonce: ByteArray): ByteArray {
        reader.selectEF("0017".hexToByteArray())
        return reader.signature(nonce)
    }

    fun signCertSignature(nonce: ByteArray): ByteArray {
        reader.selectEF("001A".hexToByteArray())
        return reader.signature(nonce)
    }
}
