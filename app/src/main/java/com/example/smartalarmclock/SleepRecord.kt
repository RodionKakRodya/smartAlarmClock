package com.example.smartalarmclock

import java.time.Duration
import java.time.Instant

data class SleepRecord(
    val start: Instant,
    val end: Instant,
) {
    val duration: Duration = Duration.between(start, end)
}
