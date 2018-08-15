package com.example.tylerwalker.buyyouadrink.activity.map

import android.app.Activity
import android.util.Log
import android.view.View
import android.widget.TextView
import com.example.tylerwalker.buyyouadrink.R
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.Marker

class PlaceInfoWindowAdapter(val context: Activity): GoogleMap.InfoWindowAdapter {
    override fun getInfoContents(marker: Marker?): View {
        val view = context.layoutInflater.inflate(R.layout.info_window, null)

        val titleTextView = view.findViewById<TextView>(R.id.title_text)
        titleTextView.text = marker?.title.toString()

        return view
    }

    override fun getInfoWindow(p0: Marker?): View? {
        return null
    }

}