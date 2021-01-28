package com.abdoul.myweather

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.abdoul.myweather.databinding.ActivityMainBinding
import com.abdoul.myweather.model.currentWeather.CurrentWeatherModel
import com.abdoul.myweather.model.enums.WeatherTypes
import com.abdoul.myweather.model.forecast.ForecastModel
import com.abdoul.myweather.other.AppUtility
import com.abdoul.myweather.other.ForecastAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import pub.devrel.easypermissions.AppSettingsDialog
import pub.devrel.easypermissions.EasyPermissions
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity(), EasyPermissions.PermissionCallbacks {

    private val mainViewModel: MainViewModel by viewModels()
    private lateinit var binding: ActivityMainBinding
    private lateinit var forecastAdapter: ForecastAdapter

    @Inject
    lateinit var appUtility: AppUtility

    private var isDataFetched = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        requestPermissions()

        if (appUtility.isLocationEnabled()) {
            getWeather()
        } else {
            appUtility.showMessage(getString(R.string.turn_location_on))
            val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            startActivity(intent)
        }

        binding.forecastFab.setOnClickListener {
            mainViewModel.getWeatherInfo(true)
        }

        forecastAdapter = ForecastAdapter(this)
        binding.forecastRecycleView.layoutManager = LinearLayoutManager(this)
        binding.forecastRecycleView.setHasFixedSize(true)
        binding.forecastRecycleView.adapter = forecastAdapter

    }

    override fun onResume() {
        super.onResume()
        if (appUtility.isLocationEnabled() && appUtility.hasLocationPermission(this) && !isDataFetched) {
            getWeather()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.refresh -> {
                if (appUtility.isLocationEnabled() && appUtility.hasLocationPermission(this)) {
                    mainViewModel.getWeatherInfo()
                } else {
                    appUtility.showMessage(getString(R.string.turn_location_on))
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun getWeather() {
        mainViewModel.getWeatherInfo()

        lifecycleScope.launchWhenStarted {
            mainViewModel.weatherState.collect {
                when (it) {
                    is MainViewModel.ViewAction.Loading -> showHideProgress(it.showProgress)
                    is MainViewModel.ViewAction.Forecast -> provideForecast(it.forecast)
                    is MainViewModel.ViewAction.CurrentWeather -> provideCurrentWeather(it.currentWeather)
                    else -> Unit
                }
            }
        }
        isDataFetched = true
    }

    private fun showHideProgress(showProgress: Boolean) {
        binding.weatherProgress.isVisible = showProgress
    }

    private fun provideCurrentWeather(weather: Result<CurrentWeatherModel>) {
        if (weather.getOrNull() != null) {
            showCurrentWeather(weather.getOrNull()!!)
        } else {
            showErrorMessage()
        }
    }

    private fun showErrorMessage() {
        if (!appUtility.isOnline())
            appUtility.showSnackBarMessage(binding.container, getString(R.string.network_error))
        else
            appUtility.showSnackBarMessage(binding.container, getString(R.string.generic_error))
    }

    private fun provideForecast(forecast: Result<ForecastModel>) {
        if (forecast.getOrNull() != null) {
            showForecast(forecast.getOrNull()!!)
        } else {
            showErrorMessage()
        }
    }

    private fun showForecast(forecast: ForecastModel) {
        forecastAdapter.setData(forecast.list)
        binding.clForeCastWeather.isVisible = true
        binding.forecastFab.isVisible = false
        binding.txtShowForecast.isVisible = false
    }

    private fun showCurrentWeather(currentWeather: CurrentWeatherModel) {
        changeBackground(currentWeather.weather[0].main)
        binding.clCurrentWeather.isVisible = true
        binding.tvCurrentLocation.text = currentWeather.name
        binding.tvTemperature.text =
            getString(
                R.string.current_temp,
                currentWeather.main.temp.toInt().toString(),
                "\u2103"
            )
        binding.tvTemperatureDetail.text = currentWeather.weather[0].main

        binding.tvMinTemp.text = getString(
            R.string.current_min_temp,
            currentWeather.main.temp_min.toInt().toString(),
            "\u2103"
        )
        binding.tvCurrentTemp.text = getString(
            R.string.current_temp,
            currentWeather.main.temp.toInt().toString(),
            "\u2103"
        )
        binding.tvMaxTemp.text = getString(
            R.string.current_max_temp,
            currentWeather.main.temp_max.toInt().toString(),
            "\u2103"
        )
    }

    private fun changeBackground(weatherType: String) {
        when (weatherType) {
            WeatherTypes.CLEAR.types -> {
                binding.clCurrentWeather.background =
                    ContextCompat.getDrawable(this, R.drawable.sea_cloudy)
                binding.clForeCastWeather.setBackgroundColor(
                    ContextCompat.getColor(this, R.color.cloudy)
                )
            }

            WeatherTypes.CLOUD.types -> {
                binding.clCurrentWeather.background =
                    ContextCompat.getDrawable(this, R.drawable.forest_cloudy)
                binding.clForeCastWeather.setBackgroundColor(
                    ContextCompat.getColor(this, R.color.cloudy)
                )
            }
            WeatherTypes.RAIN.types -> {
                binding.clCurrentWeather.background =
                    ContextCompat.getDrawable(this, R.drawable.forest_rainy)
                binding.clForeCastWeather.setBackgroundColor(
                    ContextCompat.getColor(this, R.color.rainy)
                )
            }
            else -> {
                binding.clCurrentWeather.background =
                    ContextCompat.getDrawable(this, R.drawable.forest_sunny)
                binding.clForeCastWeather.setBackgroundColor(
                    ContextCompat.getColor(this, R.color.sunny)
                )
            }
        }
    }

    private fun requestPermissions() {
        if (appUtility.hasLocationPermission(this)) {
            return
        } else {
            EasyPermissions.requestPermissions(
                this,
                "You need to accept location permission to use this app",
                AppUtility.REQUEST_CODE_LOCATION_PERMISSION,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>) {
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            AppSettingsDialog.Builder(this).setThemeResId(R.style.AlertDialogTheme).build().show()
        } else {
            requestPermissions()
        }
    }

    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>) {
    }
}