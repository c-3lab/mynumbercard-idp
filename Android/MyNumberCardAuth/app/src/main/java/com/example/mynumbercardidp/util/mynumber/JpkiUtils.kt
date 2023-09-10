package com.example.mynumbercardidp.util.mynumber

import com.example.mynumbercardidp.util.hexToByteArray
import java.nio.ByteBuffer

class JpkiUtils(private val reader: NfcReader) {
    companion object {
        private const val logTag = "JpkiUtils"
        private const val headerSize = 4 //データサイズなどの情報部が格納されている先頭部のバイト数
    }

    fun lookupAuthPin(efid: String): Int {
        reader.selectEF(efid.hexToByteArray())
        return reader.lookupPin()
    }

    fun verifyAuthPin(efid: String, pin: String): Boolean {
        reader.selectEF(efid.hexToByteArray())
        return reader.verify(pin)
    }

    fun lookupSignPin(efid: String): Int {
        reader.selectEF(efid.hexToByteArray())
        return reader.lookupPin()
    }

    fun verifySignPin(efid: String, pin: String): Boolean {
        reader.selectEF(efid.hexToByteArray())
        return reader.verify(pin)
    }

    fun readCertificateUserVerification(efid: String): ByteArray {
        return readCertificate(efid.hexToByteArray())
    }

    fun readCertificateSign(efid: String): ByteArray {
        return readCertificate(efid.hexToByteArray())
    }

    private fun readCertificate(efid: ByteArray): ByteArray {
        reader.selectEF(efid)

        // 読み込むべきサイズを取得するため、先頭4バイト取得
        val header = reader.readBinary(headerSize)
        if (header.isEmpty()) { return byteArrayOf() }

        // 読み込んだデータから、データ全体量の格納部分を抽出
        val bodySize = ByteBuffer.wrap(header, 2, 2).short

        // 全体を読み込み、データを返却する
        return readBinary(headerSize + bodySize)
    }

    private fun readBinary(expectedSize: Int): ByteArray {
        var data = byteArrayOf()

        // 不足サイズ分が取り切れるまでREAD BINARYを繰り返す
        while (data.size < expectedSize) {
            var currentData = reader.readBinary(expectedSize - data.size, data.size.toUShort())
            if (currentData.isEmpty()) { break }
            data += currentData
        }

        return data
    }

    fun authSignature(efid: String, commandArg: String, nonce: ByteArray): ByteArray {
        reader.selectEF(efid.hexToByteArray())
        return reader.signature(commandArg.toUShort(16), nonce)
    }

    fun signCertSignature(efid: String, commandArg: String, nonce: ByteArray): ByteArray {
        reader.selectEF(efid.hexToByteArray())
        return reader.signature(commandArg.toUShort(16), nonce)
    }
}
