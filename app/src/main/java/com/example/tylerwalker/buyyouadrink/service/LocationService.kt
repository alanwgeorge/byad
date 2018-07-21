package com.example.tylerwalker.buyyouadrink.service

import android.content.Context
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices

class LocationService {
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    fun getLocationClient(context: Context): FusedLocationProviderClient {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        return fusedLocationClient
    }
}