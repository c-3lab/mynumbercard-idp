package com.example.mynumbercardidp.util.mynumber

class APDUException(private val sw1: Byte, private val sw2: Byte): Exception(
    String.format("APDU command was not executed normally. The return value are as follows. sw1:%s sw2:%s",
        sw1.toString(),
        sw2.toString()
    )
)


