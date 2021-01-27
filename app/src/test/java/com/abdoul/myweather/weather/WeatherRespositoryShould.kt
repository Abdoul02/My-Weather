package com.abdoul.myweather.weather

import com.abdoul.myweather.model.currentWeather.CurrentWeatherModel
import com.abdoul.myweather.model.forecast.ForecastModel
import com.abdoul.myweather.repository.WeatherRepository
import com.abdoul.myweather.utils.BaseUnitTest
import com.mapbox.mapboxsdk.geometry.LatLng
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Test
import java.lang.RuntimeException

@ExperimentalCoroutinesApi
class WeatherRepositoryShould : BaseUnitTest() {

    private val service: WeatherService = mock()
    private val latLng: LatLng = mock()
    private val currentWeatherModel: CurrentWeatherModel = mock()
    private val forecastModel: ForecastModel = mock()
    private val exception = RuntimeException("Something went wrong")

    @Test
    fun getCurrentWeatherFromService() = runBlockingTest {
        val repository = mockCurrentWeatherCase()

        repository.getCurrentWeather(latLng)

        verify(service, times(1)).fetchCurrentWeather(latLng)
    }

    @Test
    fun getForecastFromService() = runBlockingTest {
        val repository = mockForecastCase()

        repository.getForecast(latLng)

        verify(service, times(1)).fetchForecast(latLng)
    }

    @Test
    fun emitCurrentWeatherFromService() = runBlockingTest {
        val repository = mockCurrentWeatherCase()

        assertEquals(currentWeatherModel, repository.getCurrentWeather(latLng).first().getOrNull())
    }

    @Test
    fun emitForecastFromService() = runBlockingTest {
        val repository = mockForecastCase()

        assertEquals(forecastModel, repository.getForecast(latLng).first().getOrNull())
    }

    @Test
    fun propagatesErrorsForCurrentWeather() = runBlockingTest {
        val repository = mockCurrentWeatherCase(false)

        repository.getCurrentWeather(latLng)

        assertEquals(exception, repository.getCurrentWeather(latLng).first().exceptionOrNull())
    }

    @Test
    fun propagatesErrorsForForecast() = runBlockingTest {
        val repository = mockForecastCase(false)

        repository.getCurrentWeather(latLng)

        assertEquals(exception, repository.getForecast(latLng).first().exceptionOrNull())
    }

    private suspend fun mockCurrentWeatherCase(success: Boolean = true): WeatherRepository {
        whenever(service.fetchCurrentWeather(latLng)).thenReturn(
            flow {
                if (success)
                    emit(Result.success(currentWeatherModel))
                else
                    emit(Result.failure<CurrentWeatherModel>(exception))
            }
        )

        return WeatherRepository(service)
    }

    private suspend fun mockForecastCase(success: Boolean = true): WeatherRepository {
        whenever(service.fetchForecast(latLng)).thenReturn(
            flow {
                if (success)
                    emit(Result.success(forecastModel))
                else
                    emit(Result.failure<ForecastModel>(exception))
            }
        )

        return WeatherRepository(service)
    }

}