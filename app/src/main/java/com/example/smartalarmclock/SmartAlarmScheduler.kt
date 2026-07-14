package com.example.smartalarmclock

import android.content.Context
import android.content.Intent
import android.provider.AlarmClock
import java.time.Instant
import java.time.ZoneId

class SmartAlarmScheduler(private val context: Context) {
    fun schedule(alarmAt: Instant) {
        val alarmTime = alarmAt.atZone(ZoneId.systemDefault()).toLocalTime()
        val alarmIntent = Intent(AlarmClock.ACTION_SET_ALARM).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
            putExtra(AlarmClock.EXTRA_HOUR, alarmTime.hour)
            putExtra(AlarmClock.EXTRA_MINUTES, alarmTime.minute)
            putExtra(AlarmClock.EXTRA_MESSAGE, "Smart Alarm Clock")
            putExtra(AlarmClock.EXTRA_SKIP_UI, true)
        }
        context.startActivity(alarmIntent)
    }
}
