package com.example.weatherreport.workers

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import com.example.weatherreport.R
import com.example.weatherreport.data.WeatherKeys
import kotlinx.coroutines.delay

/**
 * Финальный этап: собирает результаты параллельных worker'ов и формирует сводку.
 */
class FinalizeReportWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val cities = inputData.getStringArray(WeatherKeys.KEY_CITY) ?: emptyArray()
        val temperatures = inputData.getIntArray(WeatherKeys.KEY_TEMP) ?: IntArray(0)

        setForeground(getForegroundInfo("Все данные получены, формируем отчёт…"))
        delay(1_500)

        if (cities.isEmpty() || temperatures.isEmpty()) {
            return Result.failure()
        }

        val avgTemp = temperatures.average().toInt()
        val doneCities = cities.joinToString()

        setForeground(getForegroundInfo("Отчёт готов! Средняя температура ${formatTemp(avgTemp)}°C"))

        return Result.success(
            Data.Builder()
                .putString(WeatherKeys.KEY_DONE_CITIES, doneCities)
                .putInt(WeatherKeys.KEY_AVG_TEMP, avgTemp)
                .build()
        )
    }

    private fun getForegroundInfo(message: String): ForegroundInfo {
        ensureChannel()
        val notification: Notification = NotificationCompat.Builder(applicationContext, WeatherKeys.CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_weather)
            .setContentTitle("Сбор прогноза")
            .setContentText(message)
            .setOngoing(true)
            .build()

        return ForegroundInfo(WeatherKeys.NOTIFICATION_ID, notification)
    }

    private fun formatTemp(t: Int): String = if (t >= 0) "+$t" else "$t"

    private fun ensureChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                WeatherKeys.CHANNEL_ID,
                "Weather Report",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }
}
