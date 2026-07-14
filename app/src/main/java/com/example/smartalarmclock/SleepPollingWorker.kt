package com.example.smartalarmclock

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import java.time.Duration
import java.time.Instant
import java.util.concurrent.TimeUnit

class SleepPollingWorker(
    appContext: Context,
    workerParams: WorkerParameters,
) : CoroutineWorker(appContext, workerParams) {
    override suspend fun doWork(): Result {
        val repository = SleepRepository(applicationContext)
        val sleepStart = repository.findLatestUnprocessedSleepStart() ?: return Result.success()

        val alarmAt = sleepStart.plus(SLEEP_OFFSET)
        SmartAlarmScheduler(applicationContext).schedule(alarmAt)
        repository.markSleepProcessed(sleepStart)
        return Result.success()
    }

    companion object {
        private val SLEEP_OFFSET: Duration = Duration.ofMinutes(450)
        private const val WORK_NAME = "huawei_sleep_polling"

        fun enqueue(context: Context) {
            val request = PeriodicWorkRequestBuilder<SleepPollingWorker>(15, TimeUnit.MINUTES).build()
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.UPDATE,
                request,
            )
        }
    }
}
