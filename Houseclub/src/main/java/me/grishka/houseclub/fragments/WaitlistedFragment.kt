package me.grishka.houseclub.fragments

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import me.grishka.appkit.Nav
import me.grishka.houseclub.R
import me.grishka.houseclub.api.ClubhouseSession

class WaitlistedFragment : BaseToolbarFragment() {
    override fun onAttach(activity: Activity) {
        super.onAttach(activity)
        setTitle(R.string.waitlist)
    }

    override fun onCreateContentView(inflater: LayoutInflater, container: ViewGroup, savedInstanceState: Bundle): View {
        val view = inflater.inflate(R.layout.waitlist, container, false)
        view.findViewById<View>(R.id.log_out).setOnClickListener { v: View -> logOut(v) }
        return view
    }

    private fun logOut(v: View) {
        ClubhouseSession.userToken = null
        ClubhouseSession.userID = ClubhouseSession.userToken
        ClubhouseSession.write()
        Nav.goClearingStack(activity, LoginFragment::class.java, null)
    }
}