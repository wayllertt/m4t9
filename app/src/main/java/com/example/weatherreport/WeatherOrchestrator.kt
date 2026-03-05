package com.example.weatherreport

import android.content.Context
import androidx.work.ArrayCreatingInputMerger
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkContinuation
import androidx.work.WorkManager
import com.example.weatherreport.data.WeatherKeys
import com.example.weatherreport.workers.FetchCityWeatherWorker
import com.example.weatherreport.workers.FinalizeReportWorker

class WeatherOrchestrator(private val context: Context) {

    private val workManager = WorkManager.getInstance(context)

    fun startParallelCollection(cities: List<String>) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val fetchRequests = cities.map { city ->
            OneTimeWorkRequestBuilder<FetchCityWeatherWorker>()
                .setConstraints(constraints)
                .setInputData(Data.Builder().putString(WeatherKeys.KEY_CITY, city).build())
                .addTag("weather_fetch")
                .build()
        }

        val finalizeRequest = OneTimeWorkRequestBuilder<FinalizeReportWorker>()
            .setInputMerger(ArrayCreatingInputMerger::class)
            .addTag("weather_finalize")
            .build()

        val continuation: WorkContinuation = workManager.beginUniqueWork(
            WeatherKeys.UNIQUE_WORK_NAME,
            ExistingWorkPolicy.REPLACE,
            fetchRequests
        )

        continuation.then(finalizeRequest).enqueue()
    }
}
