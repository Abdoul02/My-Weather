package com.abdoul.myweather.weather

import com.abdoul.myweather.MainViewModel
import com.abdoul.myweather.model.currentWeather.CurrentWeatherModel
import com.abdoul.myweather.model.forecast.ForecastModel
import com.abdoul.myweather.other.LocationProvider
import com.abdoul.myweather.repository.WeatherRepository
import com.abdoul.myweather.utils.BaseUnitTest
import com.mapbox.mapboxsdk.geometry.LatLng
import com.nhaarman.mockitokotlin2.*
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Before
import org.junit.Test
import java.lang.RuntimeException

@ExperimentalCoroutinesApi
class MainViewModelShould : BaseUnitTest() {

    private val repository: WeatherRepository = mock()
    private val currentWeatherModel: CurrentWeatherModel = mock()
    private val forecastModel: ForecastModel = mock()
    private val latLng: LatLng = mock()

    private val locationProvider = mock<LocationProvider>()
    private val state = MutableStateFlow<LocationProvider.LocationData>(
        LocationProvider.LocationData.LocationRetrieved(latLng)
    )

    private val expectedForecast = Result.success(forecastModel)
    private val expectedCurrentWeather = Result.success(currentWeatherModel)
    private val exception = RuntimeException("Something went wrong")

    private val currentWeatherViewAction =
        MainViewModel.ViewAction.CurrentWeather(expectedCurrentWeather)
    private val forecastViewAction = MainViewModel.ViewAction.Forecast(expectedForecast)

    private val currentWeatherErrorViewAction = MainViewModel.ViewAction.CurrentWeather(Result.failure(exception))
    private val forecastErrorViewAction = MainViewModel.ViewAction.Forecast(Result.failure(exception))

    @Before
    fun setUp() {
        whenever(locationProvider.locationState).thenReturn(state)
    }

    @Test
    fun getCurrentWeatherFromRepository() = runBlockingTest {
        val viewModel = mockCurrentWeatherCase()

        viewModel.getWeatherInfo()
        viewModel.weatherState.first()

        verify(repository, times(1)).getCurrentWeather(latLng)
    }

    @Test
    fun getForecastFromRepository() = runBlockingTest {
        val viewModel = mockForecastCase()

        viewModel.getWeatherInfo(true)
        viewModel.weatherState.first()

        verify(repository, times(1)).getForecast(latLng)
    }

    @Test
    fun emitsCurrentWeatherFromRepositoryViaViewAction() = runBlockingTest {
        val viewModel = mockCurrentWeatherCase()

        viewModel.getWeatherInfo()
        viewModel.weatherState.first()

        assertEquals(currentWeatherViewAction, viewModel.weatherState.first())
    }

    @Test
    fun emitsForecastFromRepositoryViaViewAction() = runBlockingTest {
        val viewModel = mockForecastCase()

        viewModel.getWeatherInfo(true)
        viewModel.weatherState.first()

        assertEquals(forecastViewAction, viewModel.weatherState.first())
    }

    @Test
    fun emitCurrentWeatherErrorWhenReceived() = runBlockingTest {
        val viewModel = mockCurrentWeatherCase(false)

        viewModel.getWeatherInfo()

        assertEquals(currentWeatherErrorViewAction,viewModel.weatherState.first())
    }

    @Test
    fun emitForecastErrorWhenReceived() = runBlockingTest {
        val viewModel = mockForecastCase(false)

        viewModel.getWeatherInfo(true)

        assertEquals(forecastErrorViewAction,viewModel.weatherState.first())
    }

    private suspend fun mockCurrentWeatherCase(success: Boolean = true): MainViewModel {
        whenever(repository.getCurrentWeather(latLng)).thenReturn(
            flow {
                if (success)
                    emit(expectedCurrentWeather)
                else
                    emit(Result.failure<CurrentWeatherModel>(exception))
            }
        )

        return MainViewModel(repository, locationProvider)
    }

    private suspend fun mockForecastCase(success: Boolean = true): MainViewModel {
        whenever(repository.getForecast(latLng)).thenReturn(
            flow {
                if (success)
                    emit(expectedForecast)
                else
                    emit(Result.failure<ForecastModel>(exception))
            }
        )

        return MainViewModel(repository, locationProvider)
    }
}