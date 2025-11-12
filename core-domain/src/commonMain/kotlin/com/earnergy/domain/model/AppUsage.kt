package com.earnergy.domain.model

import kotlin.time.Duration

data class AppUsage(
    val packageName: String,
    val displayName: String,
    val category: AppCategory,
    val totalForeground: Duration
)
