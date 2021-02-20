package me.grishka.houseclub.fragments

import android.app.Activity
import me.grishka.appkit.api.SimpleCallback
import me.grishka.houseclub.R
import me.grishka.houseclub.api.methods.GetFollowers

class FollowersFragment : UserListFragment() {
    override fun onAttach(activity: Activity) {
        super.onAttach(activity)
        setTitle(R.string.followers_title)
    }

    override fun doLoadData(offset: Int, count: Int) {
        currentRequest = GetFollowers(arguments.getInt("id"), 50, offset / 50)
            .setCallback(object : SimpleCallback<GetFollowers.Response?>(this) {
                override fun onSuccess(result: GetFollowers.Response?) {
                    currentRequest = null
                    onDataLoaded(result!!.users, data.size + preloadedData.size + result.users!!.size < result.count)
                }
            })
            .exec()
    }
}