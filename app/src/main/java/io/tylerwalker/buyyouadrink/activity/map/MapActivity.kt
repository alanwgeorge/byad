package io.tylerwalker.buyyouadrink.activity.map

import android.content.Context
import android.content.SharedPreferences
import android.graphics.*
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.*
import com.google.gson.Gson
import com.yelp.fusion.client.models.SearchResponse
import kotlinx.android.synthetic.main.activity_map.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import android.support.v4.graphics.drawable.DrawableCompat
import android.support.v4.content.res.ResourcesCompat
import android.support.annotation.ColorInt
import android.support.annotation.DrawableRes
import android.widget.Toast
import com.google.android.gms.maps.model.BitmapDescriptor
import io.tylerwalker.buyyouadrink.model.Coordinates
import io.tylerwalker.buyyouadrink.model.Drink
import io.tylerwalker.buyyouadrink.model.User
import io.tylerwalker.buyyouadrink.module.App
import io.tylerwalker.buyyouadrink.service.LocationService
import io.tylerwalker.buyyouadrink.service.YelpService
import javax.inject.Inject
import io.tylerwalker.buyyouadrink.R


class MapActivity : AppCompatActivity(), OnMapReadyCallback {
    private val SHARED_PREFERENCES_CURRENT_USER_KEY = "current_user"
    private lateinit var sharedPreferences: SharedPreferences

    private var targetUser: User? = null
    private var sourceUser: User? = null

    private val manager = supportFragmentManager
    private var invitationFragment: InvitationFragment? = null

    val visibleDrinks = mutableListOf(Drink.Coffee, Drink.Juice, Drink.Beer, Drink.BubbleTea)

    private val yelpAPI = YelpService().api

    private var markers = mutableMapOf<Drink, MutableList<Marker>>(
            Drink.Coffee to mutableListOf(),
            Drink.Juice to mutableListOf(),
            Drink.Beer to mutableListOf(),
            Drink.BubbleTea to mutableListOf()
    )
    private var sourceMarker: Marker? = null
    private var targetMarker: Marker? = null

    private var map: GoogleMap? = null
    private val maxZoomPreference = 14.5F

    private val component = App().getComponent(this)

    @Inject
    lateinit var locationService: LocationService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)
        component.inject(this)

        sharedPreferences = getSharedPreferences("io.tylerwalker.buyyouadrink", Context.MODE_PRIVATE)

        sourceUser = Gson().fromJson(sharedPreferences.getString(SHARED_PREFERENCES_CURRENT_USER_KEY, null), User::class.java)
        targetUser = intent.getParcelableExtra("user")

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(this)

        resetDrinkDrawableFilters()
    }

    private fun resetDrinkDrawableFilters() {
        getDrawable(R.drawable.ic_coffee)?.clearColorFilter()
        getDrawable(R.drawable.ic_beer)?.clearColorFilter()
        getDrawable(R.drawable.ic_juice)?.clearColorFilter()
        getDrawable(R.drawable.ic_bubble_tea)?.clearColorFilter()
    }

    override fun onMapReady(googleMap: GoogleMap?) {
        map = googleMap

        val builder = LatLngBounds.builder()

        if (targetUser == null) {
            targetUser = User("0", Coordinates(37.8716F, -122.2727F), drinks = "Coffee")
        }

        if (sourceUser == null) {
            sourceUser = User("1", Coordinates(37.86F, -122.2755F), drinks = "BubbleTea")
        }

        sourceUser?.let {
            val sourceLoc = LatLng(it.location.latitude.toDouble(), it.location.longitude.toDouble())
            sourceMarker = map?.addMarker(MarkerOptions().position(sourceLoc).title(it.display_name).icon(BitmapDescriptorFactory.defaultMarker()))
        }

        targetUser?.let {
            val targetLoc = LatLng(it.location.latitude.toDouble(), it.location.longitude.toDouble())
            targetMarker = map?.addMarker(MarkerOptions().position(targetLoc).title(it.display_name).icon(BitmapDescriptorFactory.defaultMarker()))
            builder.include(targetMarker?.position)
        }

        val bounds = builder.build()
        map?.setMaxZoomPreference(maxZoomPreference)
        map?.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 200))

        // set up search params
        val params = buildSearchParams()

        val infoWindowAdapter = PlaceInfoWindowAdapter(this)
        map?.setInfoWindowAdapter(infoWindowAdapter)
        map?.setOnInfoWindowClickListener {
            it.hideInfoWindow()
            showInvitation(it)
        }

        val call = yelpAPI.getBusinessSearch(params)
        call.enqueue(ResponseHandler())

        coffee_button.setOnClickListener { toggleCategory(Drink.Coffee) }
        beer_button.setOnClickListener { toggleCategory(Drink.Beer) }
        bubble_tea_button.setOnClickListener { toggleCategory(Drink.BubbleTea) }
        juice_button.setOnClickListener { toggleCategory(Drink.Juice) }
    }

    private fun buildSearchParams(): MutableMap<String, String> {
        val params: MutableMap<String, String> = mutableMapOf()

        params["categories"] = "beer_and_wine,coffee,juicebars,bubbletea"
        params["latitude"] = targetUser?.location?.latitude.toString()
        params["longitude"] = targetUser?.location?.longitude.toString()
        params["limit"] = "50"
//        params["sort_by"] = "rating"

        return params
    }


    inner class ResponseHandler: Callback<SearchResponse> {
        override fun onFailure(call: Call<SearchResponse>?, t: Throwable?) {
            Toast.makeText(this@MapActivity, "There was a problem searching for businesses in your area...", Toast.LENGTH_SHORT).show()
            Log.e("yelp", "error ${t?.cause}")
        }

        override fun onResponse(call: Call<SearchResponse>?, response: Response<SearchResponse>?) {
            val res = response?.body()

            res?.let {
                res.businesses.run {
                    shuffle()
                    dropLast(25).forEach { business ->

                        val lat = business.coordinates.latitude
                        val long = business.coordinates.longitude

                        val markerOptions = MarkerOptions()
                                .position(LatLng(lat, long))
                                .title(business.name)
                                .snippet(business.rating.toString())

                        business.categories.map { category ->
                            when (category.alias) {
                                "coffee" -> addMarkerToCoffee(markerOptions)
                                "beer_and_wine" -> addMarkerToBeer(markerOptions)
                                "bubbletea" -> addMarkerToBubbleTea(markerOptions)
                                "juicebars" -> addMarkerToJuice(markerOptions)
                                else -> { }
                            }
                        }
                    }
                }

                toggleCategories()

                targetUser?.drinks?.let {
                    if (it.isEmpty()) toggleCategories()
                    it.split(",").forEach {
                        when (it) {
                            "Coffee" -> toggleCategory(Drink.Coffee)
                            "Juice" -> toggleCategory(Drink.Juice)
                            "Beer" -> toggleCategory(Drink.Beer)
                            "BubbleTea" -> toggleCategory(Drink.BubbleTea)
                            else -> {}
                        }
                    }
                } ?: toggleCategories()
            }
        }
    }

    private fun addMarkerToCoffee(markerOptions: MarkerOptions) {
        markerOptions.icon(vectorToBitmap(R.drawable.ic_coffee_marker))

        map?.run {
            addMarker(markerOptions).apply {
                markers[Drink.Coffee]?.add(this)
                tag = Drink.Coffee.name
            }
        }
    }

    private fun addMarkerToBeer(markerOptions: MarkerOptions) {
        markerOptions.icon(vectorToBitmap(R.drawable.ic_beer_marker))

        map?.run {
            addMarker(markerOptions).apply {
                markers[Drink.Beer]?.add(this)
                tag = Drink.Beer.name
            }
        }
    }

    private fun addMarkerToBubbleTea(markerOptions: MarkerOptions) {
        markerOptions.icon(vectorToBitmap(R.drawable.ic_bubble_tea_marker))

        map?.run {
            addMarker(markerOptions).apply {
                markers[Drink.BubbleTea]?.add(this)
                tag = Drink.BubbleTea.name
            }
        }
    }

    private fun addMarkerToJuice(markerOptions: MarkerOptions) {
        markerOptions.icon(vectorToBitmap(R.drawable.ic_juice_marker))

        map?.run {
            addMarker(markerOptions).apply {
                markers[Drink.Juice]?.add(this)
                tag = Drink.Juice.name
            }
        }
    }

    fun getMidpoint(userA: User, userB: User): Coordinates {
        val locA = userA.location
        val locB = userB.location

        return Coordinates((locA.latitude + locB.latitude)/2, (locA.longitude + locB.longitude)/2)
    }

    private fun toggleCategories() {
        toggleCategory(Drink.Juice)
        toggleCategory(Drink.Coffee)
        toggleCategory(Drink.Beer)
        toggleCategory(Drink.BubbleTea)
    }

    fun toggleCategory(drink: Drink) {
        if (visibleDrinks.contains(drink)) {
            visibleDrinks.remove(drink)

            markers[drink]?.forEach {
                it.isVisible = false
            }

            when (drink) {
                Drink.Coffee -> coffee_button.setCardBackgroundColor(Color.WHITE)
                Drink.BubbleTea -> bubble_tea_button.setCardBackgroundColor(Color.WHITE)
                Drink.Beer -> beer_button.setCardBackgroundColor(Color.WHITE)
                Drink.Juice -> juice_button.setCardBackgroundColor(Color.WHITE)
            }

        } else {
            visibleDrinks.add(drink)

            markers[drink]?.forEach {
                it.isVisible = true
            }

            when (drink) {
                Drink.Coffee -> coffee_button.setCardBackgroundColor(Color.parseColor("#FF3B00"))
                Drink.BubbleTea -> bubble_tea_button.setCardBackgroundColor(Color.parseColor("#FF3B00"))
                Drink.Beer -> beer_button.setCardBackgroundColor(Color.parseColor("#FF3B00"))
                Drink.Juice -> juice_button.setCardBackgroundColor(Color.parseColor("#FF3B00"))
            }
        }
    }

    private fun showInvitation(marker: Marker) {
        invitationFragment?.let {
            manager.beginTransaction().remove(it).commit()
        }

        invitationFragment = InvitationFragment().apply {
            arguments = buildBundle(marker)

            supportFragmentManager.beginTransaction()
                    .add(this, "invitation")
                    .commit()
        }
    }

    fun dismissInvitation() {
        invitationFragment?.let {
            manager.beginTransaction().remove(it).commit()
        }

        invitationFragment = null
    }

    private fun buildBundle(marker: Marker): Bundle? = Bundle().apply {
        putString("placeName", marker.title)
        putString("beverageType", marker.tag as String)
        putString("locationName", locationService.getLocationName(this@MapActivity, Coordinates(marker.position.latitude.toFloat(), marker.position.longitude.toFloat())))
        putFloat("latitude", marker.position.latitude.toFloat())
        putFloat("longitude", marker.position.longitude.toFloat())
        putString("inviteeName", targetUser?.display_name)
        putString("inviteeId", targetUser?.user_id)
        putString("inviteeImage", targetUser?.profile_image)
    }

    private fun vectorToBitmap(@DrawableRes id: Int, @ColorInt color: Int? = null): BitmapDescriptor {
        val vectorDrawable = ResourcesCompat.getDrawable(resources, id, null)
        val bitmap = Bitmap.createBitmap(vectorDrawable!!.intrinsicWidth / 3,
                vectorDrawable.intrinsicHeight /3, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        vectorDrawable.setBounds(0, 0, canvas.width, canvas.height)
        color?.let { DrawableCompat.setTint(vectorDrawable, it) }
        vectorDrawable.draw(canvas)
        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }
}
