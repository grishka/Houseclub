package me.grishka.houseclub.api

import android.content.Context
import android.content.SharedPreferences
import me.grishka.houseclub.App
import me.grishka.houseclub.api.model.User
import java.util.Locale
import java.util.UUID

object ClubhouseSession {
    var deviceID: String? = null
    var userID: String? = null
    var userToken: String? = null
    var isWaitlisted = false
    var self: User? = null
    fun load() {
        val prefs = prefs()
        deviceID = prefs.getString("device_id", null)
        userID = prefs.getString("user_id", null)
        userToken = prefs.getString("user_token", null)
        isWaitlisted = prefs.getBoolean("waitlisted", false)
        if (deviceID == null) {
            deviceID = UUID.randomUUID().toString().toUpperCase(Locale.getDefault())
            write()
        }
    }

    fun write() {
        prefs().edit()
            .putString("device_id", deviceID)
            .putString("user_id", userID)
            .putString("user_token", userToken)
            .putBoolean("waitlisted", isWaitlisted)
            .apply()
    }

    val isLoggedIn: Boolean
        get() = userID != null

    private fun prefs(): SharedPreferences {
        return App.applicationContext.getSharedPreferences("session", Context.MODE_PRIVATE)
    }
}