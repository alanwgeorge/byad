package com.example.tylerwalker.buyyouadrink.activity.profile

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.tylerwalker.buyyouadrink.R
import android.graphics.drawable.ColorDrawable
import android.util.Log
import android.widget.Toast
import com.example.tylerwalker.buyyouadrink.databinding.FavoriteDrinkDialogBinding
import kotlinx.android.synthetic.main.favorite_drink_dialog.*


class FavoriteDrinkDialog: DialogFragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val binding = DataBindingUtil.inflate<FavoriteDrinkDialogBinding>(inflater, R.layout.favorite_drink_dialog, container, false)
        binding.setLifecycleOwner(this)
        activity?.let {
            ViewModelProviders.of(it).get(SetupProfileViewModel::class.java).apply {
                binding.viewModel = this

                favoriteDrink.observe(this@FavoriteDrinkDialog, Observer {
                    Log.d("FavoriteDrinkDialog", "favorite drink: $it")
                    it?.let { drink ->
                        val drinks = listOf(beer_button, coffee_button, juice_button, bubble_tea_button)
                        drinks.forEach { imageButton ->
                            if (imageButton.contentDescription == drink.name) {
                                Log.d("FavoriteDrinkDialog", "setBackgroundColor() ${imageButton.contentDescription}")
                                imageButton.drawable.setTint(resources.getColor(R.color.altAccent, null))
                                Toast.makeText(getApplication(), drink.name, Toast.LENGTH_SHORT).show()
                            } else {
                                imageButton.drawable.setTint(resources.getColor(android.R.color.black, null))
                            }
                        }
                    }
                })
            }
        }

        return binding.root
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
    }
}