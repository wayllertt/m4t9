package com.example.weatherreport.data

data class CityWeatherResult(
    val city: String,
    val temperatureC: Int,
    val condition: String
)

object WeatherKeys {
    const val KEY_CITY = "key_city"
    const val KEY_TEMP = "key_temp"
    const val KEY_CONDITION = "key_condition"
    const val KEY_DONE_CITIES = "key_done_cities"
    const val KEY_IN_PROGRESS_CITIES = "key_in_progress_cities"
    const val KEY_AVG_TEMP = "key_avg_temp"
    const val UNIQUE_WORK_NAME = "parallel_weather_report"
    const val CHANNEL_ID = "weather_report_channel"
    const val NOTIFICATION_ID = 9001
}
