package com.abdoul.myweather.other

import android.annotation.SuppressLint
import android.location.Location
import android.os.Looper
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.mapbox.mapboxsdk.geometry.LatLng
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocationProvider @Inject constructor(
    private val fusedLocationProviderClient: FusedLocationProviderClient,
    private val locationRequest: LocationRequest,
    private val appUtility: AppUtility
) {
    init {
        getCurrentLocation()
    }

    private val _locationState = MutableStateFlow<LocationData>(LocationData.Empty)
    val locationState: StateFlow<LocationData> = _locationState

    @SuppressLint("MissingPermission")
    private fun getCurrentLocation() {
        if (appUtility.isLocationEnabled()) {
            fusedLocationProviderClient.lastLocation.addOnCompleteListener { task ->
                val location: Location? = task.result
                if (location == null) {
                    fusedLocationProviderClient.requestLocationUpdates(
                        locationRequest, mLocationCallback,
                        Looper.myLooper()
                    )
                } else {
                    _locationState.value = LocationData.LocationRetrieved(LatLng(location))
                }
            }
        }
    }

    private val mLocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            val location: Location = locationResult.lastLocation
            _locationState.value = LocationData.LocationRetrieved(LatLng(location))
        }
    }

    sealed class LocationData {
        object Empty : LocationData()
        data class LocationRetrieved(val latLng: LatLng) : LocationData()
    }
}