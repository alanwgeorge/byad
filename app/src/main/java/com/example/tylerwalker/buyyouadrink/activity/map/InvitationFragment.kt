package com.example.tylerwalker.buyyouadrink.activity.map

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.tylerwalker.buyyouadrink.R
import android.support.v4.app.DialogFragment
import android.util.Log
import android.widget.Button
import com.example.tylerwalker.buyyouadrink.R.drawable.user
import com.example.tylerwalker.buyyouadrink.activity.home.HomeScreen
import com.example.tylerwalker.buyyouadrink.activity.messages.MessagesActivity
import com.example.tylerwalker.buyyouadrink.databinding.InvitationBinding
import com.example.tylerwalker.buyyouadrink.model.Coordinates
import com.example.tylerwalker.buyyouadrink.model.InvitationEvent
import com.example.tylerwalker.buyyouadrink.module.App
import io.reactivex.Flowable
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class InvitationFragment: DialogFragment() {

    @Inject
    lateinit var invitationEventsFlowable: Flowable<InvitationEvent>

    private var compositeDisposable = CompositeDisposable()

    private val logTag = "InvitationFragment"

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val binding = DataBindingUtil.inflate <InvitationBinding> (inflater, R.layout.invitation, container, true)
        binding.setLifecycleOwner(this@InvitationFragment)

        val component = App().getComponent(context!!)
        component.inject(this)

        ViewModelProviders.of(this).get(InvitationViewModel::class.java).apply {
            lifecycle.addObserver(this)
            component.inject(this)

            inviteeName.value = arguments?.getString("inviteeName")
            inviteeId.value = arguments?.getString("inviteeId")
            inviteeImage.value = arguments?.getString("inviteeImage")
            beverageType.value = arguments?.getString("beverageType")
            locationName.value = arguments?.getString("locationName")
            arguments?.run { location.value = Coordinates(getFloat("latitude"), getFloat("longitude")) }
            placeName.value = arguments?.getString("placeName")

            confirmVisibility.value = View.VISIBLE
            loadingVisibility.value = View.GONE
            confirmVisibility.value = View.GONE


            stage.observe(this@InvitationFragment, Observer {
                Log.d("InvitationFragment", "stage: $it")

                when (it) {
                    InvitationViewModel.Companion.InvitationStage.Failure -> {
                        Single.timer(2L, TimeUnit.SECONDS).doOnSuccess {
                            transitionToHome()
                        }
                                .subscribe()
                    }
                    InvitationViewModel.Companion.InvitationStage.Confirm -> {
                        Single.timer(2L, TimeUnit.SECONDS).doOnSuccess {
                            transitionToMessages()
                        }
                                .subscribe()
                    }
                }
            })

            confirmVisibility.observe(this@InvitationFragment, Observer {
                Log.d("InvitationFragment", "confirm visibility: $it")
            })

            loadingVisibility.observe(this@InvitationFragment, Observer {
                Log.d("InvitationFragment", "loading visibility: $it")
            })

            invitationVisibility.observe(this@InvitationFragment, Observer {
                Log.d("InvitationFragment", "invitation visibility: $it")
            })

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
                setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            }
        }
    }

    override fun onStop() {
        super.onStop()

        compositeDisposable.clear()
        compositeDisposable = CompositeDisposable()
    }

    fun transitionToMessages() {
        val intent = Intent(this.context, MessagesActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        startActivity(intent)
    }

    fun transitionToHome() {
        val intent = Intent(this.context, HomeScreen::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        startActivity(intent)
    }
}