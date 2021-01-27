package com.abdoul.myweather.repository

import com.abdoul.myweather.model.currentWeather.CurrentWeatherModel
import com.abdoul.myweather.model.forecast.ForecastModel
import com.abdoul.myweather.weather.WeatherService
import com.mapbox.mapboxsdk.geometry.LatLng
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class WeatherRepository @Inject constructor(
    private val service: WeatherService
) {

    suspend fun getCurrentWeather(latLng: LatLng): Flow<Result<CurrentWeatherModel>> =
        service.fetchCurrentWeather(latLng).map {
            if (it.isSuccess)
                Result.success(it.getOrNull()!!)
            else
                Result.failure(it.exceptionOrNull()!!)
        }

    suspend fun getForecast(latLng: LatLng): Flow<Result<ForecastModel>> =
        service.fetchForecast(latLng).map {
            if (it.isSuccess)
                Result.success(it.getOrNull()!!)
            else
                Result.failure(it.exceptionOrNull()!!)
        }
}