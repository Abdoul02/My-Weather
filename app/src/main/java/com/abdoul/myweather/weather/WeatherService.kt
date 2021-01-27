package com.abdoul.myweather.weather

import com.abdoul.myweather.model.currentWeather.CurrentWeatherModel
import com.abdoul.myweather.model.forecast.ForecastModel
import com.abdoul.myweather.other.AppUtility.Companion.WEATHER_API_KEY
import com.abdoul.myweather.other.AppUtility.Companion.WEATHER_UNIT
import com.mapbox.mapboxsdk.geometry.LatLng
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class WeatherService @Inject constructor(
    private val api: WeatherApi
) {

    suspend fun fetchCurrentWeather(latLng: LatLng): Flow<Result<CurrentWeatherModel>> {
        return flow {
            emit(
                Result.success(
                    api.getCurrentWeather(
                        latitude = latLng.latitude,
                        longitude = latLng.longitude,
                        metric = WEATHER_UNIT,
                        key = WEATHER_API_KEY
                    )
                )
            )
        }.catch {
            emit(Result.failure(RuntimeException("Something went wrong")))
        }
    }

    suspend fun fetchForecast(latLng: LatLng): Flow<Result<ForecastModel>> {
        return flow {
            emit(
                Result.success(
                    api.getForecastWeather(
                        latitude = latLng.latitude,
                        longitude = latLng.longitude,
                        metric = WEATHER_UNIT,
                        key = WEATHER_API_KEY
                    )
                )
            )
        }.catch {
            emit(Result.failure(RuntimeException("Something went wrong")))
        }
    }
}