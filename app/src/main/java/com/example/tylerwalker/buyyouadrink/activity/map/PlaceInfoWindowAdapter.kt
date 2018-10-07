package com.example.tylerwalker.buyyouadrink.activity.map

import android.app.Activity
import android.media.Rating
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.RatingBar
import android.widget.TextView
import com.example.tylerwalker.buyyouadrink.R
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.Marker

class PlaceInfoWindowAdapter(private val mapActivity: MapActivity): GoogleMap.InfoWindowAdapter {
    override fun getInfoContents(marker: Marker?): View {
        val view = mapActivity.layoutInflater.inflate(R.layout.info_window, null)

        val titleTextView = view.findViewById<TextView>(R.id.title_text)
        titleTextView.text = marker?.title.toString()

        view.findViewById<RatingBar>(R.id.ratingBar).run {
            rating = marker?.snippet?.toFloat() ?: 3.0F
        }

        view.findViewById<Button>(R.id.primary_button).run {
            text = resources.getString(R.string.choose_here)
        }

        return view
    }

    override fun getInfoWindow(p0: Marker?): View? {
        return null
    }

}