package com.example.tylerwalker.buyyouadrink.service

import android.content.Context
import android.location.Address
import android.location.Geocoder
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
       return "San Francisco"
    }
}