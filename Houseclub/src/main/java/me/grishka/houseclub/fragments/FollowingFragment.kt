package me.grishka.houseclub.fragments

import android.app.Activity
import me.grishka.appkit.api.SimpleCallback
import me.grishka.houseclub.R
import me.grishka.houseclub.api.methods.GetFollowing

class FollowingFragment : UserListFragment() {
    override fun onAttach(activity: Activity) {
        super.onAttach(activity)
        setTitle(R.string.following_title)
    }

    override fun doLoadData(offset: Int, count: Int) {
        currentRequest = GetFollowing(arguments.getInt("id"), 50, offset / 50)
            .setCallback(object : SimpleCallback<GetFollowing.Response?>(this) {
                override fun onSuccess(result: GetFollowing.Response?) {
                    currentRequest = null
                    onDataLoaded(result!!.users, data.size + preloadedData.size + result.users!!.size < result.count)
                }
            })
            .exec()
    }
}