package com.royaraqamia.rabwa.keyboard

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class KeyboardLayoutTest {

    @Test
    fun `arabic row structures contain required characters`() {
        assertTrue(arabicRow1.contains("ض"))
        assertTrue(arabicRow2.contains("ا"))
        assertTrue(arabicRow3.contains("ة"))
        assertTrue(tashkeel.contains("َ"))
        assertTrue(arabicHamzaAndPunctuation.contains("أ"))
        assertTrue(arabicHamzaAndPunctuation.contains("إ"))
        assertTrue(arabicHamzaAndPunctuation.contains("آ"))
    }

    @Test
    fun `english row structures contain full alphabet`() {
        assertEquals(10, englishRow1.size)
        assertEquals(9, englishRow2.size)
        assertEquals(7, englishRow3.size)

        val fullAlphabet = (englishRow1 + englishRow2 + englishRow3).sorted()
        val expected = ('a'..'z').map { it.toString() }.sorted()
        assertEquals(expected, fullAlphabet)
    }

    @Test
    fun `symbols row structures contain required numbers and punctuation`() {
        assertEquals(10, symbolsRow1.size)
        assertTrue(symbolsRow1.contains("1"))
        assertTrue(symbolsRow2.contains("@"))
        assertTrue(symbolsRow3.contains("؟"))
    }
}
