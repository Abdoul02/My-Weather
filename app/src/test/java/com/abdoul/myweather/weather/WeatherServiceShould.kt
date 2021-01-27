package com.abdoul.myweather.weather

import com.abdoul.myweather.model.currentWeather.CurrentWeatherModel
import com.abdoul.myweather.model.forecast.ForecastModel
import com.abdoul.myweather.other.AppUtility
import com.abdoul.myweather.utils.BaseUnitTest
import com.mapbox.mapboxsdk.geometry.LatLng
import com.nhaarman.mockitokotlin2.*
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Test
import java.lang.RuntimeException

@ExperimentalCoroutinesApi
class WeatherServiceShould : BaseUnitTest() {

    private lateinit var service: WeatherService
    private var api: WeatherApi = mock()
    private val latLng: LatLng = mock()
    private val currentWeatherModel: CurrentWeatherModel = mock()
    private val forecastModel: ForecastModel = mock()

    @Test
    fun getCurrentWeatherFromAPI() = runBlockingTest {
        service = WeatherService(api)

        service.fetchCurrentWeather(latLng).first()

        verify(api, times(1)).getCurrentWeather(
            0.0, 0.0, AppUtility.WEATHER_UNIT,
            AppUtility.WEATHER_API_KEY
        )
    }

    @Test
    fun getForecastFromAPI() = runBlockingTest {
        service = WeatherService(api)

        service.fetchForecast(latLng).first()

        verify(api, times(1)).getForecastWeather(
            0.0, 0.0, AppUtility.WEATHER_UNIT,
            AppUtility.WEATHER_API_KEY
        )
    }

    @Test
    fun convertValuesToFlowResultAndEmitsThem() = runBlockingTest {
        whenever(
            api.getCurrentWeather(
                0.0, 0.0, AppUtility.WEATHER_UNIT,
                AppUtility.WEATHER_API_KEY
            )
        ).thenReturn(currentWeatherModel)

        whenever(
            api.getForecastWeather(
                0.0, 0.0, AppUtility.WEATHER_UNIT,
                AppUtility.WEATHER_API_KEY
            )
        ).thenReturn(forecastModel)

        service = WeatherService(api)

        assertEquals(
            Result.success(currentWeatherModel),
            service.fetchCurrentWeather(latLng).first()
        )

        assertEquals(
            Result.success(forecastModel),
            service.fetchForecast(latLng).first()
        )
    }

    @Test
    fun emitsErrorResultWhenNetworkFails() = runBlockingTest {
        whenever(
            api.getForecastWeather(
                0.0, 0.0, AppUtility.WEATHER_UNIT,
                AppUtility.WEATHER_API_KEY
            )
        ).thenThrow(RuntimeException("Oh oh network error"))

        whenever(
            api.getCurrentWeather(
                0.0, 0.0, AppUtility.WEATHER_UNIT,
                AppUtility.WEATHER_API_KEY
            )
        ).thenThrow(RuntimeException("Oh oh network error"))

        service = WeatherService(api)

        assertEquals(
            "Something went wrong",
            service.fetchForecast(latLng).first().exceptionOrNull()?.message
        )
        assertEquals(
            "Something went wrong",
            service.fetchCurrentWeather(latLng).first().exceptionOrNull()?.message
        )

    }
}