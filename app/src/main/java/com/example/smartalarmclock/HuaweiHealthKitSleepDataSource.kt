package com.example.smartalarmclock

import android.content.Context
import com.huawei.hms.hihealth.DataController
import com.huawei.hms.hihealth.HiHealth
import com.huawei.hms.hihealth.data.DataType
import com.huawei.hms.hihealth.options.DataReadOptions
import kotlinx.coroutines.tasks.await
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.concurrent.TimeUnit

class HuaweiHealthKitSleepDataSource(context: Context) {
    private val dataController: DataController = HiHealth.getDataController(context)

    suspend fun latestSleepStart(): Instant? = sleepRecords().maxByOrNull { it.start }?.start

    suspend fun sleepRecords(hoursBack: Long = 36): List<SleepRecord> {
        val now = Instant.now()
        val readOptions = DataReadOptions.Builder()
            .read(DataType.DT_CONTINUOUS_SLEEP)
            .setTimeRange(now.minus(hoursBack, ChronoUnit.HOURS).toEpochMilli(), now.toEpochMilli(), TimeUnit.MILLISECONDS)
            .build()

        val response = dataController.read(readOptions).await()
        return response.dataSets
            .asSequence()
            .flatMap { it.dataPoints.asSequence() }
            .map { dataPoint ->
                SleepRecord(
                    start = Instant.ofEpochMilli(dataPoint.getStartTime(TimeUnit.MILLISECONDS)),
                    end = Instant.ofEpochMilli(dataPoint.getEndTime(TimeUnit.MILLISECONDS)),
                )
            }
            .sortedByDescending { it.start }
            .toList()
    }
}
