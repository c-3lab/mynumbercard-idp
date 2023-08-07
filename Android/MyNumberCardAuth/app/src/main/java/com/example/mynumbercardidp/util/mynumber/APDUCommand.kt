package com.example.mynumbercardidp.util.mynumber

class APDUCommand(val command: ByteArray) {
    companion object {
        fun apduCase1(cla: Byte, ins: Byte, p1: Byte, p2: Byte): APDUCommand {
            return APDUCommand(byteArrayOf(cla, ins, p1, p2))
        }

        fun apduCase2(cla: Byte, ins: Byte, p1: Byte, p2: Byte, le: Int): APDUCommand {
            return if (le <= 256) APDUCommand(byteArrayOf(cla, ins, p1, p2, le.toByte()))
            else APDUCommand(byteArrayOf(cla, ins, p1, p2, 0, le.shr(8).toByte(), le.toByte()))
        }

        fun apduCase3(cla: Byte, ins: Byte, p1: Byte, p2: Byte, data: ByteArray): APDUCommand {
            val lc = data.size
            return if (lc <= 256) {
                APDUCommand(byteArrayOf(cla, ins, p1, p2, lc.toByte()) + data)
            } else {
                APDUCommand(byteArrayOf(cla, ins, p1, p2, 0, lc.shr(8).toByte(), lc.toByte()) + data)
            }
        }

        fun apduCase4(cla: Byte, ins: Byte, p1: Byte, p2: Byte, data: ByteArray, le: Int): APDUCommand {
            val lc = data.size
            return if (lc <= 256 && le <= 256) {
                APDUCommand(byteArrayOf(cla, ins, p1, p2, lc.toByte()) + data + le.toByte())
            } else {
                APDUCommand(
                    byteArrayOf(cla, ins, p1, p2, 0, lc.shr(8).toByte(), lc.toByte())
                            + data
                            + byteArrayOf(le.shr(8).toByte(), le.toByte())
                )
            }
        }
    }
}
