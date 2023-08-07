package com.example.mynumbercardidp.util.mynumber

import com.example.mynumbercardidp.util.hexToByteArray
import com.example.mynumbercardidp.util.toHexString

class JpkiUtils(private val reader: NfcReader) {
    companion object {
        private const val logTag = "JpkiUtils"
        private const val offset = 4 //読み込むべきサイズを取得するための情報部が格納されているバイト数
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
        val header = reader.readBinary(offset)
        // 読み込んだデータから、データ全体量の格納部分を抽出
        val sizeToReadHex = header.toHexString().substring(4,8)
        val sizeToRead = Integer.parseInt(sizeToReadHex, 16) + offset

        // 全体を読み込み、データを返却する
        return reader.readBinary(sizeToRead)
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
