package com.example.tylerwalker.buyyouadrink.model

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.example.tylerwalker.buyyouadrink.module.App
import com.google.android.gms.flags.impl.SharedPreferencesFactory.getSharedPreferences
import com.google.gson.Gson

class LocalStorage(val context: Context) {
    private val sharedPreferences: SharedPreferences by lazy {
        val prefs = context.getSharedPreferences("com.example.tylerwalker.buyyouadrink", Context.MODE_PRIVATE)
        prefs
    }

    private val SHARED_PREFERENCES_CURRENT_USER_KEY = "current_user"
    private val SHARED_PREFERENCES_FIRST_RUN_KEY = "first_run"

    fun getCurrentUser(): User? = Gson().fromJson(sharedPreferences.getString(SHARED_PREFERENCES_CURRENT_USER_KEY, null), User::class.java)
    fun setCurrentUser(user: User?) {
        Log.d("LocalStorage", "put user: $user")
        val json = Gson().toJson(user)
        sharedPreferences.edit().putString(SHARED_PREFERENCES_CURRENT_USER_KEY, json).apply()
        Log.d("LocalStorage", "put user success: $json")
    }
    fun isFirstRun(): Boolean = sharedPreferences.getBoolean(SHARED_PREFERENCES_FIRST_RUN_KEY, false)
    fun setFirstRun(isFirstRun: Boolean = false) = sharedPreferences?.edit().putBoolean(SHARED_PREFERENCES_FIRST_RUN_KEY, isFirstRun)







    fun putString(key: String, value: String) {
        sharedPreferences.edit().putString(key, value).apply()
    }
    fun getString(key: String): String? = sharedPreferences.getString(key, null)
}