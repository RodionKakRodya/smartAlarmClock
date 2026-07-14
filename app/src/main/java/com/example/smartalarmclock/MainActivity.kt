package com.example.smartalarmclock

import android.Manifest
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class MainActivity : AppCompatActivity() {
    private val dateFormatter = DateTimeFormatter.ofPattern("dd.MM HH:mm").withZone(ZoneId.systemDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val status = TextView(this).apply {
            text = getString(R.string.status_text)
            textSize = 16f
        }
        val sleepTable = TableLayout(this).apply {
            setStretchAllColumns(true)
        }
        val startButton = Button(this).apply {
            text = getString(R.string.start_polling)
            setOnClickListener {
                SleepPollingWorker.enqueue(applicationContext)
                status.text = getString(R.string.polling_enabled)
            }
        }
        val loadSleepButton = Button(this).apply {
            text = getString(R.string.load_sleep_data)
            setOnClickListener {
                lifecycleScope.launch {
                    status.text = getString(R.string.loading_sleep_data)
                    runCatching { SleepRepository(applicationContext).sleepRecords() }
                        .onSuccess { records ->
                            status.text = if (records.isEmpty()) getString(R.string.sleep_table_empty) else getString(R.string.sleep_table_loaded, records.size)
                            renderSleepTable(sleepTable, records)
                        }
                        .onFailure { error ->
                            status.text = getString(R.string.sleep_table_error, error.localizedMessage ?: error.javaClass.simpleName)
                        }
                }
            }
        }

        setContentView(
            LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(48, 72, 48, 48)
                addView(status)
                addView(startButton)
                addView(loadSleepButton)
                addView(sleepTable)
            }
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), 1001)
        }
    }

    private fun renderSleepTable(table: TableLayout, records: List<SleepRecord>) {
        table.removeAllViews()
        table.addView(row("Начало", "Конец", "Минут"))
        records.forEach { record ->
            table.addView(
                row(
                    dateFormatter.format(record.start),
                    dateFormatter.format(record.end),
                    record.duration.toMinutes().toString(),
                )
            )
        }
    }

    private fun row(first: String, second: String, third: String): TableRow = TableRow(this).apply {
        addView(cell(first))
        addView(cell(second))
        addView(cell(third))
    }

    private fun cell(value: String): TextView = TextView(this).apply {
        text = value
        setPadding(8, 8, 8, 8)
    }
}
