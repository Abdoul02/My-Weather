package com.abdoul.myweather

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import com.abdoul.myweather.other.LocationProvider
import com.abdoul.myweather.repository.WeatherRepository
import androidx.lifecycle.viewModelScope
import com.abdoul.myweather.model.currentWeather.CurrentWeatherModel
import com.abdoul.myweather.model.forecast.ForecastModel
import com.mapbox.mapboxsdk.geometry.LatLng
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MainViewModel @ViewModelInject constructor(
    private val repository: WeatherRepository,
    private val locationProvider: LocationProvider
) : ViewModel() {

    private val _weatherState = MutableStateFlow<ViewAction>(ViewAction.Empty)
    val weatherState: StateFlow<ViewAction> = _weatherState

    fun getWeatherInfo(isForecast: Boolean = false) {
        _weatherState.value = ViewAction.Loading(true)
        viewModelScope.launch {
            locationProvider.locationState.collect {
                when (it) {
                    is LocationProvider.LocationData.LocationRetrieved -> {
                        if (isForecast)
                            getForecast(it.latLng)
                        else
                            getCurrentWeather(it.latLng)
                    }
                    else -> Unit
                }
            }
        }
    }

    private fun getForecast(latLng: LatLng) {
        viewModelScope.launch {
            repository.getForecast(latLng)
                .onEach {
                    _weatherState.value = ViewAction.Loading(false)
                }.collect {
                    _weatherState.value = ViewAction.Forecast(it)
                }
        }
    }

    private fun getCurrentWeather(latLng: LatLng) {
        viewModelScope.launch {
            repository.getCurrentWeather(latLng)
                .onEach {
                    _weatherState.value = ViewAction.Loading(false)
                }
                .collect {
                    _weatherState.value = ViewAction.CurrentWeather(it)
                }
        }
    }

    sealed class ViewAction {
        data class Loading(val showProgress: Boolean) : ViewAction()
        data class Forecast(val forecast: Result<ForecastModel>) : ViewAction()
        data class CurrentWeather(val currentWeather: Result<CurrentWeatherModel>) : ViewAction()
        object Empty : ViewAction()
    }
}