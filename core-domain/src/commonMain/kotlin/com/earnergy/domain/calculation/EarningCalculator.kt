package com.earnergy.domain.calculation

import com.earnergy.domain.model.AppCategory
import com.earnergy.domain.model.DaySummary
import com.earnergy.domain.model.MoneyImpact
import kotlin.math.round

object EarningCalculator {
    fun computeImpact(daySummary: DaySummary, productivePackages: Set<String>): MoneyImpact {
        val totalSeconds = daySummary.usages.sumOf { it.totalForeground.inWholeSeconds }
        val productiveSeconds = daySummary.usages
            .filter { it.packageName in productivePackages || it.category == AppCategory.PRODUCTIVE }
            .sumOf { it.totalForeground.inWholeSeconds }

        val passiveSeconds = (totalSeconds - productiveSeconds).coerceAtLeast(0)
        val productiveHours = productiveSeconds / 3600.0
        val passiveHours = passiveSeconds / 3600.0

        val potentialEarnings = roundToTwoDecimals(productiveHours * daySummary.hourlyRate)
        val potentialLoss = roundToTwoDecimals(passiveHours * daySummary.hourlyRate)

        return MoneyImpact(
            productiveSeconds = productiveSeconds,
            passiveSeconds = passiveSeconds,
            potentialEarningsUsd = potentialEarnings,
            potentialLossUsd = potentialLoss
        )
    }

    private fun roundToTwoDecimals(value: Double): Double {
        return round(value * 100) / 100
    }
}
