package com.example.tylerwalker.buyyouadrink.activity.map

import android.arch.lifecycle.MutableLiveData
import android.content.Context
import android.content.SharedPreferences
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import com.example.tylerwalker.buyyouadrink.R
import com.example.tylerwalker.buyyouadrink.model.Coordinates
import com.example.tylerwalker.buyyouadrink.model.Drink
import com.example.tylerwalker.buyyouadrink.model.User
import com.example.tylerwalker.buyyouadrink.service.YelpService
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.*
import com.google.gson.Gson
import com.yelp.fusion.client.models.SearchResponse
import kotlinx.android.synthetic.main.activity_map.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class MapActivity : AppCompatActivity(), OnMapReadyCallback {
    private val SHARED_PREFERENCES_CURRENT_USER_KEY = "current_user"
    private var targetUser: User? = null
    private var sourceUser: User? = null
    private lateinit var sharedPreferences: SharedPreferences
    val visibleDrinks = mutableListOf(Drink.Coffee, Drink.Juice, Drink.Beer, Drink.BubbleTea)

    private val yelpAPI = YelpService().api

    private var markers = mutableMapOf(
            Drink.Coffee to mutableListOf<Marker>(),
            Drink.Juice to mutableListOf<Marker>(),
            Drink.Beer to mutableListOf<Marker>(),
            Drink.BubbleTea to mutableListOf<Marker>()
    )
    private var sourceMarker: Marker? = null
    private var targetMarker: Marker? = null

    private var map: GoogleMap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)

        sharedPreferences = getSharedPreferences("com.example.tylerwalker.buyyouadrink", Context.MODE_PRIVATE)

        sourceUser = Gson().fromJson(sharedPreferences.getString(SHARED_PREFERENCES_CURRENT_USER_KEY, null), User::class.java)
        targetUser = intent.getParcelableExtra("user")

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap?) {
        map = googleMap

        val builder = LatLngBounds.builder()

        if (targetUser == null) {
            targetUser = User("0", "?", "?", Coordinates(37.8716F, -122.2727F), "Coffee")
        }

        if (sourceUser == null) {
            sourceUser = User("1", "??", "??", Coordinates(37.86F, -122.2755F), "Coffee")
        }

        sourceUser?.let {
            val sourceLoc = LatLng(it.location.latitude.toDouble(), it.location.longitude.toDouble())
            sourceMarker = map?.addMarker(MarkerOptions().position(sourceLoc).title("${it.first_name} ${it.last_name}").icon(BitmapDescriptorFactory.defaultMarker()))
            builder.include(sourceMarker?.position)
        }

        targetUser?.let {
            val targetLoc = LatLng(it.location.latitude.toDouble(), it.location.longitude.toDouble())
            targetMarker = map?.addMarker(MarkerOptions().position(targetLoc).title("${it.first_name} ${it.last_name}").icon(BitmapDescriptorFactory.defaultMarker()))
            builder.include(targetMarker?.position)
        }

        val bounds = builder.build()
        map?.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 200))


        // set up search params
        val params: MutableMap<String, String> = mutableMapOf()
        params.put("categories", "beer_and_wine,coffee,juicebars,bubbletea")

        val midpoint = getMidpoint(sourceUser!!, targetUser!!)
        params.put("latitude", "${midpoint.latitude}")
        params.put("longitude", "${midpoint.longitude}")

        params.put("limit", "50")

        val infoWindowAdapter = PlaceInfoWindowAdapter(this)
        map?.setInfoWindowAdapter(infoWindowAdapter)

        val call = yelpAPI.getBusinessSearch(params)
        call.enqueue(ResponseHandler())

        wine_button.setOnClickListener { toggleCategory(Drink.BubbleTea) }
        beer_button.setOnClickListener { toggleCategory(Drink.Beer) }
        juice_button.setOnClickListener { toggleCategory(Drink.Juice) }
        coffee_button.setOnClickListener { toggleCategory(Drink.Coffee) }
    }


    inner class ResponseHandler: Callback<SearchResponse> {
        override fun onFailure(call: Call<SearchResponse>?, t: Throwable?) {
            Log.e("yelp", "error ${t?.cause}")
        }

        override fun onResponse(call: Call<SearchResponse>?, response: Response<SearchResponse>?) {
            val res = response?.body()

//            Log.d("yelp", "businesses ${res?.businesses.toString()}")

            res?.let {
                res.businesses.forEach {

                    val lat = it.coordinates.latitude
                    val long = it.coordinates.longitude
                    val marker = map?.addMarker(MarkerOptions().position(LatLng(lat, long)).title(it.name).snippet(it.categories.toString()))
//                    Log.d("marker", "name: ${it.name}")
                    it.categories.map {
//                        Log.d("marker", "category: ${it.title}, alias: ${it.alias}")
                        when (it.alias) {
                            "coffee" -> marker?.let { it.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.coffee)); markers[Drink.Coffee]?.add(it) }
                            "beer_and_wine" -> marker?.let {  it.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.beer)); markers[Drink.Beer]?.add(it) }
                            "juicebars" -> marker?.let {  it.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.juice));markers[Drink.Juice]?.add(it) }
                            "bubbletea" -> marker?.let {  it.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.wine));markers[Drink.BubbleTea]?.add(it) }
                            else -> {}
                        }
                    }

//                    markers.toList().map { Log.d("marker", "category: ${it.first.name}, count: ${it.second.count()}")}

                }
            }
        }
    }

    fun getMidpoint(userA: User, userB: User): Coordinates {
        val locA = userA.location
        val locB = userB.location

        return Coordinates((locA.latitude + locB.latitude)/2, (locA.longitude + locB.longitude)/2)
    }

    fun toggleCategory(drink: Drink) {
        if (visibleDrinks.contains(drink)) {
            visibleDrinks.remove(drink)

            markers[drink]?.forEach {
                it.isVisible = false
            }
        } else {
            visibleDrinks.add(drink)

            markers[drink]?.forEach {
                it.isVisible = true
            }
        }
    }


}
