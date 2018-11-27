package io.tylerwalker.buyyouadrink.service

import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.util.Log
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import io.tylerwalker.buyyouadrink.model.Coordinates

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
        try {
            val list = geocoder.getFromLocation(lat.toDouble(), long.toDouble(), 1)
            if (list != null && list.size > 0) {
                Log.d("getLocationName()", "list:  $list")

                val address: Address = list[0]
                Log.d("getLocationName()", "address: $address")

                val result = address.locality
                Log.d("getLocationName()", "locality: $result")

                return result
            }
        } catch(e: Throwable) {
            return null
        }


        return null
    }

    fun getLocationAddress(context: Context, coordinates: Coordinates): String? {
        val geocoder = Geocoder(context)
        val (lat, long) = coordinates
        val list = geocoder.getFromLocation(lat.toDouble(), long.toDouble(), 1)
        if (list != null && list.size > 0) {
            val address: Address = list[0]

            return address.getAddressLine(0)
        }

        return null
    }

    fun addressToCoordinates(context: Context, address: String): Coordinates? {
        val geocoder = Geocoder(context)
        val loc = geocoder.getFromLocationName(address, 1)
        loc[0]?.let {
            return Coordinates(it.latitude.toFloat(), it.longitude.toFloat())
        }
        return null
    }
}