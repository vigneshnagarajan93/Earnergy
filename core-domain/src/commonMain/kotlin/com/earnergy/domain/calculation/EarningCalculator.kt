package com.earnergy.domain.calculation

import com.earnergy.domain.model.AppCategory
import com.earnergy.domain.model.DaySummary
import com.earnergy.domain.model.MoneyImpact
import kotlin.math.round

object EarningCalculator {
    fun computeImpact(daySummary: DaySummary): MoneyImpact {
        val productiveSeconds = daySummary.usages
            .filter { it.role == com.earnergy.domain.model.AppRole.INVESTED }
            .sumOf { it.totalForeground.inWholeSeconds }

        val passiveSeconds = daySummary.usages
            .filter { it.role == com.earnergy.domain.model.AppRole.DRIFT }
            .sumOf { it.totalForeground.inWholeSeconds }

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
