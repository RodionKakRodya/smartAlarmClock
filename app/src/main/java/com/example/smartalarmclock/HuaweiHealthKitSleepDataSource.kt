package com.example.smartalarmclock

import android.content.Context
import com.huawei.hmf.tasks.Task
import com.huawei.hms.hihealth.DataController
import com.huawei.hms.hihealth.HuaweiHiHealth
import com.huawei.hms.hihealth.data.DataType
import com.huawei.hms.hihealth.options.ReadOptions
import kotlinx.coroutines.suspendCancellableCoroutine
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class HuaweiHealthKitSleepDataSource(context: Context) {
    private val dataController: DataController = HuaweiHiHealth.getDataController(context)

    suspend fun latestSleepStart(): Instant? {
        val now = Instant.now()
        val readOptions = ReadOptions.Builder()
            .read(DataType.DT_CONTINUOUS_SLEEP)
            .setTimeRange(now.minus(36, ChronoUnit.HOURS).toEpochMilli(), now.toEpochMilli(), TimeUnit.MILLISECONDS)
            .build()

        val response = dataController.read(readOptions).awaitHuaweiTask()
        return response.sampleSets
            .asSequence()
            .flatMap { sampleSet -> sampleSet.samplePoints.asSequence() }
            .map { samplePoint -> Instant.ofEpochMilli(samplePoint.getStartTime(TimeUnit.MILLISECONDS)) }
            .maxOrNull()
    }
}

private suspend fun <T> Task<T>.awaitHuaweiTask(): T = suspendCancellableCoroutine { continuation ->
    addOnSuccessListener { result ->
        continuation.resume(result)
    }
    addOnFailureListener { exception ->
        continuation.resumeWithException(exception)
    }
}
