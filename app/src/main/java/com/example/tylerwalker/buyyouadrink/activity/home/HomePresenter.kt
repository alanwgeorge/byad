package com.example.tylerwalker.buyyouadrink.activity.home

import com.example.tylerwalker.buyyouadrink.service.LocationService

class HomePresenter {
    lateinit var locationService: LocationService
    lateinit var activity: HomeScreen

//    fun getLastLocation() {
//        val client = locationService.getLocationClient(activity)
//        try {
//            if (activity.needsPermissions()) {
//                activity.promptForPermissions()
//            }
//            @SuppressWarnings("MissingPermission")
//            client.lastLocation
//                    .addOnSuccessListener { location: Location? ->
//                        Log.d("LOCATION", "lat: ${location?.latitude}, long: ${location?.longitude}")
//                    }
//        } catch (e: Exception) {
//            Log.e("ERROR", "LOCATION ERROR: ${e.message}")
//        }
//    }


}