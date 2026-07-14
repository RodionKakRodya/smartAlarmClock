package com.example.smartalarmclock

import android.Manifest
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val status = TextView(this).apply {
            text = getString(R.string.status_text)
            textSize = 16f
        }
        val startButton = Button(this).apply {
            text = getString(R.string.start_polling)
            setOnClickListener {
                SleepPollingWorker.enqueue(applicationContext)
                status.text = getString(R.string.polling_enabled)
            }
        }

        setContentView(
            LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(48, 72, 48, 48)
                addView(status)
                addView(startButton)
            }
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), 1001)
        }
    }
}
