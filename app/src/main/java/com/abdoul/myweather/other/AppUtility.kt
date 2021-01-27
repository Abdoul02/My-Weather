package com.abdoul.myweather.other

import android.Manifest
import android.content.Context
import android.location.LocationManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.view.View
import android.widget.Toast
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.qualifiers.ApplicationContext
import pub.devrel.easypermissions.EasyPermissions
import javax.inject.Inject

class AppUtility @Inject constructor(@ApplicationContext private val context: Context) {

    companion object {
        const val WEATHER_API_KEY = "553c6868e55911a25016bd12138e0974"
        const val WEATHER_UNIT = "metric"
        const val REQUEST_CODE_LOCATION_PERMISSION = 0
    }

    fun isLocationEnabled(): Boolean {
        val locationManager: LocationManager =
            context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )
    }

    fun showMessage(msg: String) {
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
    }

    /**
     * Checks if the user accepted the necessary location permissions or not
     */
    fun hasLocationPermission(mContext: Context) = EasyPermissions.hasPermissions(
        mContext,
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )

    fun isOnline(): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkCapabilities =
            connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
        return networkCapabilities != null && networkCapabilities.hasCapability(
            NetworkCapabilities.NET_CAPABILITY_INTERNET
        ) &&
                networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }

    fun showSnackBarMessage(root: View, message: String) {
        Snackbar.make(root, message, Snackbar.LENGTH_LONG).show()
    }
}