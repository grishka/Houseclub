package me.grishka.houseclub.fragments

import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import me.grishka.appkit.fragments.ToolbarFragment

abstract class BaseToolbarFragment : ToolbarFragment() {
    override fun wantsLightStatusBar(): Boolean {
        return true
    }

    override fun wantsLightNavigationBar(): Boolean {
        return true
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        toolbar.elevation = 0f
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        toolbar.elevation = 0f
    }
}