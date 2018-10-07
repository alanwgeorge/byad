package com.example.tylerwalker.buyyouadrink.activity.map

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.databinding.DataBindingUtil
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.support.constraint.ConstraintLayout
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.tylerwalker.buyyouadrink.R
import android.support.v4.app.DialogFragment
import android.util.Log
import android.widget.Button
import com.example.tylerwalker.buyyouadrink.databinding.InvitationBinding
import kotlinx.android.synthetic.main.invitation.*

class Invitation: DialogFragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val binding = DataBindingUtil.inflate <InvitationBinding> (inflater, R.layout.invitation, container, true)
        ViewModelProviders.of(this).get(InvitationViewModel::class.java).apply {
            lifecycle.addObserver(this)

            inviteeName.value = arguments?.getString("inviteeName")
            beverageType.value = arguments?.getString("beverageType")
            locationName.value = arguments?.getString("locationName")
            placeName.value = arguments?.getString("placeName")

            isLoading.value = false
            isLoading.observe(this@Invitation, Observer {
                Log.d("loading", "loading: $it")
            })

            loadingVisibility.observe(this@Invitation, Observer { value ->
                value?.let { progress_bar.visibility = it }
            })

            invitationVisibility.observe(this@Invitation, Observer { value ->
                value?.let { invitation_form.visibility = it }
            })

            send = {
                isLoading.value.let {
                    it?.let { isLoading.postValue(!it) }
                }
//                (activity as MapActivity).dismissInvitation()
            }

            binding.root.findViewById<Button>(R.id.primary_button).apply {
                text = resources.getString(R.string.send)
                setOnClickListener { send() }
            }

            binding.viewModel = this
        }

        return binding.root
    }

    override fun onStart() {
        super.onStart()

        dialog?.run {
            window?.run {
                setLayout(1000, 1250)
            }
        }
    }
}