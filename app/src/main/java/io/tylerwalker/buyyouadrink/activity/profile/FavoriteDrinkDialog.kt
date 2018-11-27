package io.tylerwalker.buyyouadrink.activity.profile

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.databinding.DataBindingUtil
import android.graphics.PorterDuff
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.util.Log
import android.widget.Toast
import io.tylerwalker.buyyouadrink.R
import kotlinx.android.synthetic.main.favorite_drink_dialog.*
import io.tylerwalker.buyyouadrink.databinding.FavoriteDrinkDialogBinding


class FavoriteDrinkDialog: DialogFragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val binding = DataBindingUtil.inflate<FavoriteDrinkDialogBinding>(inflater, R.layout.favorite_drink_dialog, container, false)
        binding.setLifecycleOwner(this)
        activity?.let { fragmentActivity ->
            ViewModelProviders.of(fragmentActivity).get(SetupProfileViewModel::class.java).apply {
                binding.viewModel = this

                favoriteDrink.observe(this@FavoriteDrinkDialog, Observer {
                    Log.d("FavoriteDrinkDialog", "favorite drink: $it")
                    it?.let { drink ->
                        val drinks = listOf(beer_button, coffee_button, juice_button, bubble_tea_button)
                        drinks.forEach { imageButton ->
                            if (imageButton.contentDescription == drink.name) {
                                val color = fragmentActivity.getColor(R.color.colorPrimary)
                                imageButton.drawable.setColorFilter(color, PorterDuff.Mode.SRC_IN)
                                Toast.makeText(getApplication(), drink.name, Toast.LENGTH_SHORT).show()
                            } else {
                                imageButton.drawable.clearColorFilter()
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