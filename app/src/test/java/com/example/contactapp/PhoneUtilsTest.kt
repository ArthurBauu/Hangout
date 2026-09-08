package com.example.contactapp

import com.example.contactapp.util.PhoneUtils
import org.junit.Assert.assertEquals
import org.junit.Test

class PhoneUtilsTest {
    @Test
    fun testNormalize_basic() {
        assertEquals("+33123456789", PhoneUtils.normalize("0123456789"))
        assertEquals("+33123456789", PhoneUtils.normalize("0033123456789"))
        assertEquals("+33123456789", PhoneUtils.normalize("+33123456789"))
        assertEquals("+33123456789", PhoneUtils.normalize("01 23 45 67 89"))
        assertEquals("1234567", PhoneUtils.normalize("1234567"))
    }
}
