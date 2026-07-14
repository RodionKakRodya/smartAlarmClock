package com.example.smartalarmclock

import android.content.Context
import java.time.Instant

class SleepRepository(context: Context) {
    private val preferences = context.getSharedPreferences("sleep_alarm", Context.MODE_PRIVATE)
    private val healthKit = HuaweiHealthKitSleepDataSource(context)

    suspend fun findLatestUnprocessedSleepStart(): Instant? {
        val sleepStart = healthKit.latestSleepStart() ?: return null
        val processed = preferences.getString(KEY_LAST_PROCESSED_SLEEP_START, null)
        return if (processed == sleepStart.toString()) null else sleepStart
    }

    fun markSleepProcessed(sleepStart: Instant) {
        preferences.edit().putString(KEY_LAST_PROCESSED_SLEEP_START, sleepStart.toString()).apply()
    }

    suspend fun sleepRecords(): List<SleepRecord> = healthKit.sleepRecords()

    companion object {
        private const val KEY_LAST_PROCESSED_SLEEP_START = "last_processed_sleep_start"
    }
}
