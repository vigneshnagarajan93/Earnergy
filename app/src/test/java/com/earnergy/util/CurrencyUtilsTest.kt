package com.earnergy.util

import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Locale

class CurrencyUtilsTest {

    @Test
    fun testFormatCurrency_USD() {
        val formatted = CurrencyUtils.formatCurrency(50.0, "USD", Locale.US)
        // Note: NumberFormat might use non-breaking space or other variations
        // but typically it's $50.00 in US locale
        assertEquals("$50.00", formatted.replace('\u00A0', ' '))
    }

    @Test
    fun testFormatCurrency_EUR() {
        val formatted = CurrencyUtils.formatCurrency(50.0, "EUR", Locale.GERMANY)
        // In Germany, it's often 50,00 €
        assertEquals("50,00 €", formatted.replace('\u00A0', ' '))
    }

    @Test
    fun testFormatCurrency_PLN() {
        val formatted = CurrencyUtils.formatCurrency(50.0, "PLN", Locale("pl", "PL"))
        // In Poland, it's 50,00 zł
        assertEquals("50,00 zł", formatted.replace('\u00A0', ' '))
    }

    @Test
    fun testFormatCurrency_InvalidCode() {
        val formatted = CurrencyUtils.formatCurrency(50.0, "INVALID", Locale.US)
        assertEquals("INVALID 50.00", formatted)
    }
}
