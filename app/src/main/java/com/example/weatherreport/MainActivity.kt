package com.example.weatherreport

import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.example.weatherreport.data.WeatherKeys

class MainActivity : AppCompatActivity() {

    private lateinit var statusText: TextView
    private lateinit var orchestrator: WeatherOrchestrator

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        statusText = findViewById(R.id.statusText)
        val collectButton: Button = findViewById(R.id.collectButton)
        orchestrator = WeatherOrchestrator(this)

        collectButton.setOnClickListener {
            val cities = listOf("Москва", "Лондон", "Нью-Йорк")
            statusText.text = "Загружаем погоду для ${cities.size} городов…"
            pushOngoingNotification(statusText.text.toString())
            orchestrator.startParallelCollection(cities)
        }

        observeWorkStatus()
    }

    private fun observeWorkStatus() {
        WorkManager.getInstance(this)
            .getWorkInfosForUniqueWorkLiveData(WeatherKeys.UNIQUE_WORK_NAME)
            .observe(this) { infos ->
                if (infos.isNullOrEmpty()) return@observe

                val fetchDone = infos.filter { it.tags.contains("weather_fetch") && it.state == WorkInfo.State.SUCCEEDED }
                val fetchRunning = infos.filter { it.tags.contains("weather_fetch") && it.state == WorkInfo.State.RUNNING }
                val finalizeSucceeded = infos.any { it.tags.contains("weather_finalize") && it.state == WorkInfo.State.SUCCEEDED }

                val doneCities = fetchDone.mapNotNull { it.outputData.getString(WeatherKeys.KEY_CITY) }
                val runningCities = fetchRunning.mapNotNull { it.progress.getString(WeatherKeys.KEY_CITY) }

                val message = when {
                    finalizeSucceeded -> {
                        val finalWorker = infos.first { it.tags.contains("weather_finalize") }
                        val avg = finalWorker.outputData.getInt(WeatherKeys.KEY_AVG_TEMP, 0)
                        "Отчёт готов! Средняя температура ${if (avg >= 0) "+$avg" else "$avg"}°C"
                    }

                    doneCities.isNotEmpty() -> {
                        val done = doneCities.joinToString(" и ")
                        val inProgress = if (runningCities.isNotEmpty()) "${runningCities.joinToString()} в процессе…" else "завершаем…"
                        "Готово: $done, $inProgress"
                    }

                    else -> "Загружаем погоду для 3 городов…"
                }

                statusText.text = message
                pushOngoingNotification(message)
            }
    }

    private fun pushOngoingNotification(message: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                WeatherKeys.CHANNEL_ID,
                "Weather Report",
                NotificationManager.IMPORTANCE_LOW
            )
            (getSystemService(NOTIFICATION_SERVICE) as NotificationManager).createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(this, WeatherKeys.CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_weather)
            .setContentTitle("Сбор прогноза")
            .setContentText(message)
            .setOngoing(true)
            .build()

        (getSystemService(NOTIFICATION_SERVICE) as NotificationManager)
            .notify(WeatherKeys.NOTIFICATION_ID, notification)
    }
}
