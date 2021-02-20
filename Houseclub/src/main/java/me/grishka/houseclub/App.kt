package me.grishka.houseclub

import android.app.Application
import android.content.Context
import me.grishka.appkit.utils.V
import me.grishka.houseclub.api.ClubhouseSession

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        Companion.applicationContext = applicationContext
        V.setApplicationContext(Companion.applicationContext)
        ClubhouseSession.load()
    }

    companion object {
        @JvmField var applicationContext: Context? = null
    }
}