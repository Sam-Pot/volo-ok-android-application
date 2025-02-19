package com.example.volook.shared.helpers

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.lifecycle.MutableLiveData
import com.example.volook.MainActivity
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.OnSuccessListener

class LocationManager(private val context: Context) {

    private var fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

    companion object{
        val REQUEST_CODE = 100
    }

    fun getLocation(deviceLocation: MutableLiveData<DeviceLocation>) {
        checkPermission()
        fusedLocationClient.lastLocation
            .addOnSuccessListener(MainActivity.instance, OnSuccessListener<Location> { location ->
                if (location != null) {
                    val latitude = location.latitude
                    val longitude = location.longitude
                    deviceLocation.value = DeviceLocation(latitude, longitude)
                }
            })
    }

    private fun checkPermission(){
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                MainActivity.instance,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_CODE
            )
            return
        }
    }

    data class DeviceLocation(val latitude: Double, val longitude: Double)
}
