package com.earnergy.domain.model

data class DaySummary(
    val dateEpochDay: Long,
    val usages: List<AppUsage>,
    val hourlyRate: Double,
    val energyStart: Int?,
    val energyEnd: Int?
)
