package com.example.mynumbercardidp.util

import androidx.compose.ui.text.AnnotatedString
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.eq
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import java.util.Arrays

class ExtensionsKtTest {
    @Test
    fun hexToByteArray() {
        assertTrue(Arrays.equals("00000000".hexToByteArray(), byteArrayOf(0, 0, 0, 0)))
    }

    @Test
    fun toHexString() {
        assertEquals(byteArrayOf(0, 0, 0, 0).toHexString(), "00000000")
    }

    @Test
    fun onEach() = runBlocking<Unit> {
        class Dummy {
            fun action(@Suppress("UNUSED_PARAMETER") old: String, @Suppress("UNUSED_PARAMETER") new: String) {}
        }
        var dummy: Dummy = mock(Dummy::class.java)
        var flow: Flow<String> = flowOf("dummy1", "dummy2")

        flow.onEach("prev"){ old, new ->
            dummy.action(old, new)
        }.collect {}

        verify(dummy, times(1)).action(eq("prev"), eq("dummy1"))
        verify(dummy, times(1)).action(eq("dummy1"), eq("dummy2"))
        verifyNoMoreInteractions(dummy)
    }
}


class PasswordVisualTransformationExcludesLastTest {
    @Test
    fun filterTest_whenDefaultInputPassword() {
        var originalText = "dummy"

        var transformation = PasswordVisualTransformationExcludesLast()
        var annotationString = AnnotatedString(text = originalText)
        var filteredString = transformation.filter(annotationString)

        assertEquals("\u2022\u2022\u2022\u2022y", filteredString.text.toString())
    }

    @Test
    fun filterTest_whenInputPassword() {
        var originalText = "Password1"
        var mask = '*'

        var transformation = PasswordVisualTransformationExcludesLast(mask)
        var annotationString = AnnotatedString(text = originalText)
        var filteredString = transformation.filter(annotationString)

        assertEquals("********1", filteredString.text.toString())
    }

    @Test
    fun filterTest_whenInputPasswordFor1Char() {
        var originalText = "1"
        var mask = '*'

        var transformation = PasswordVisualTransformationExcludesLast(mask)
        var annotationString = AnnotatedString(text = originalText)
        var filteredString = transformation.filter(annotationString)

        assertEquals("1", filteredString.text.toString())
    }

    @Test
    fun filterTest_whenPasswordIsNull() {
        var originalText = ""
        var mask = '*'

        var transformation = PasswordVisualTransformationExcludesLast(mask)
        var annotationString = AnnotatedString(text = originalText)
        var filteredString = transformation.filter(annotationString)

        assertFalse(filteredString.text.toString().contains(mask))
    }
}