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
import kotlin.random.Random

/**
 * Имитирует сетевую загрузку погоды для конкретного города.
 * Во время работы держит foreground-notification, чтобы задача не была убита.
 */
class FetchCityWeatherWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val city = inputData.getString(WeatherKeys.KEY_CITY) ?: return Result.failure()

        setForeground(getForegroundInfo("Загружаем погоду для $city…"))
        setProgress(Data.Builder().putString(WeatherKeys.KEY_CITY, city).build())

        // Имитация непредсказуемой длительности запроса
        delay(Random.nextLong(2_000, 6_000))

        val temperature = Random.nextInt(-5, 26)
        val condition = listOf("Ясно", "Облачно", "Дождь", "Снег").random()

        return Result.success(
            Data.Builder()
                .putString(WeatherKeys.KEY_CITY, city)
                .putInt(WeatherKeys.KEY_TEMP, temperature)
                .putString(WeatherKeys.KEY_CONDITION, condition)
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
