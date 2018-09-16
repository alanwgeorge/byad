package com.example.tylerwalker.buyyouadrink.service

import android.content.Context
import android.location.Address
import android.location.Criteria
import android.location.Geocoder
import android.location.LocationManager
import android.util.Log
import com.example.tylerwalker.buyyouadrink.model.Coordinates
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices

class LocationService {
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    fun getLocationClient(context: Context): FusedLocationProviderClient {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        return fusedLocationClient
    }

    fun getLocationName(context: Context, coordinates: Coordinates): String? {
        val geocoder = Geocoder(context)
        val (lat, long) = coordinates
        Log.d("getLocationName()", "$lat, $long")
        val list = geocoder.getFromLocation(lat.toDouble(), long.toDouble(), 1)
        if (list != null && list.size > 0) {
            Log.d("getLocationName()", "list:  $list")

            val address: Address = list[0]
            Log.d("getLocationName()", "address: $address")

            val result = address.locality
            Log.d("getLocationName()", "locality: $result")

            return result
        }

        return null
    }
}