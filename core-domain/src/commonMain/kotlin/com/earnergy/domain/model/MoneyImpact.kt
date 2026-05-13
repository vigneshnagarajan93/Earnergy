package com.earnergy.domain.model

data class MoneyImpact(
    val productiveSeconds: Long,
    val passiveSeconds: Long,
    val potentialEarnings: Double,
    val potentialLoss: Double
)
