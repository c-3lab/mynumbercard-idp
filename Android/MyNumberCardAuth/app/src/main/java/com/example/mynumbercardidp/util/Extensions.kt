package com.example.mynumbercardidp.util

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

fun String.hexToByteArray(): ByteArray =
    chunked(2).map { it.toInt(16).toByte() }.toByteArray()

fun ByteArray.toHexString(): String = joinToString(separator = "") { eachByte -> "%02X".format(eachByte) }

fun <T> Flow<T>.onEach(initial: T, action: suspend (old: T, new: T) -> Unit): Flow<T> = flow {
    var prev: T = initial
    collect { value ->
        action(prev, value)
        prev = value
        emit(value)
    }
}

class PasswordVisualTransformationExcludesLast(private val mask: Char = '\u2022') : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val masked = buildAnnotatedString {
            val last = text.text.lastOrNull() // 最後の文字を確保
            if (last != null) {
                if (text.text.length >= 2) {  // 入力値が1文字の時はマスクをしない
                    append(mask.toString().repeat(text.text.length - 1))
                }
                append(last)
            }
        }
        return TransformedText(
            masked,
            OffsetMapping.Identity,
        )
    }
}
