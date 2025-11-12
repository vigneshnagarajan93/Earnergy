package com.earnergy.domain.model

data class MoneyImpact(
    val productiveSeconds: Long,
    val passiveSeconds: Long,
    val potentialEarningsUsd: Double,
    val potentialLossUsd: Double
)
