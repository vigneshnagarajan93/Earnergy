package com.earnergy.util

import java.text.NumberFormat
import java.util.Currency
import java.util.Locale

object CurrencyUtils {
    /**
     * Formats a monetary value based on the provided currency code.
     *
     * @param value The double value to format.
     * @param currencyCode The ISO 4217 currency code (e.g., "USD", "EUR").
     * @param locale The locale to use for formatting. Defaults to the system default.
     * @return A formatted currency string.
     */
    fun formatCurrency(
        value: Double,
        currencyCode: String,
        locale: Locale = Locale.getDefault()
    ): String {
        return try {
            val format = NumberFormat.getCurrencyInstance(locale)
            format.currency = Currency.getInstance(currencyCode)
            format.format(value)
        } catch (e: Exception) {
            // Fallback for invalid currency codes or other issues
            "$currencyCode ${String.format("%.2f", value)}"
        }
    }
}
