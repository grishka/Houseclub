package me.grishka.houseclub.fragments

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.res.Configuration
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.text.InputType
import android.text.TextUtils
import android.view.Gravity
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import me.grishka.appkit.Nav
import me.grishka.appkit.api.Callback
import me.grishka.appkit.api.ErrorResponse
import me.grishka.appkit.api.SimpleCallback
import me.grishka.appkit.fragments.LoaderFragment
import me.grishka.appkit.imageloader.ViewImageLoader
import me.grishka.houseclub.R
import me.grishka.houseclub.VoiceService.Companion.instance
import me.grishka.houseclub.api.BaseResponse
import me.grishka.houseclub.api.ClubhouseSession
import me.grishka.houseclub.api.methods.Follow
import me.grishka.houseclub.api.methods.GetProfile
import me.grishka.houseclub.api.methods.Unfollow
import me.grishka.houseclub.api.methods.UpdateBio
import me.grishka.houseclub.api.model.FullUser
import java.text.DateFormat

class ProfileFragment : LoaderFragment() {
    private var user: FullUser? = null
    private var name: TextView? = null
    private var username: TextView? = null
    private var followers: TextView? = null
    private var following: TextView? = null
    private var followsYou: TextView? = null
    private var bio: TextView? = null
    private var inviteInfo: TextView? = null
    private var twitter: TextView? = null
    private var instagram: TextView? = null
    private var photo: ImageView? = null
    private var inviterPhoto: ImageView? = null
    private var followBtn: Button? = null
    private var socialButtons: View? = null
    private var self = false
    override fun onAttach(activity: Activity) {
        super.onAttach(activity)
        loadData()
        self = arguments.getInt("id") == ClubhouseSession.userID!!.toInt()
        if (self) setHasOptionsMenu(true)
    }

    override fun onCreateContentView(inflater: LayoutInflater, container: ViewGroup, savedInstanceState: Bundle): View {
        val v = inflater.inflate(R.layout.profile, container, false)
        name = v.findViewById(R.id.name)
        username = v.findViewById(R.id.username)
        followers = v.findViewById(R.id.followers)
        following = v.findViewById(R.id.following)
        followsYou = v.findViewById(R.id.follows_you)
        bio = v.findViewById(R.id.bio)
        inviteInfo = v.findViewById(R.id.invite_info)
        photo = v.findViewById(R.id.photo)
        inviterPhoto = v.findViewById(R.id.inviter_photo)
        followBtn = v.findViewById(R.id.follow_btn)
        twitter = v.findViewById(R.id.twitter)
        instagram = v.findViewById(R.id.instagram)
        socialButtons = v.findViewById(R.id.social)
        followBtn?.setOnClickListener(View.OnClickListener { v: View -> onFollowClick(v) })
        instagram?.setOnClickListener(View.OnClickListener { v: View -> onInstagramClick(v) })
        twitter?.setOnClickListener(View.OnClickListener { v: View -> onTwitterClick(v) })
        followers?.setOnClickListener(View.OnClickListener { v: View -> onFollowersClick(v) })
        following?.setOnClickListener(View.OnClickListener { v: View -> onFollowingClick(v) })
        v.findViewById<View>(R.id.inviter_btn).setOnClickListener { v: View -> onInviterClick(v) }
        if (self) bio?.setOnClickListener(View.OnClickListener { v: View -> onBioClick(v) })
        return v
    }

    override fun doLoadData() {
        currentRequest = GetProfile(arguments.getInt("id"))
            .setCallback(object : SimpleCallback<GetProfile.Response?>(this) {
                override fun onSuccess(result: GetProfile.Response?) {
                    currentRequest = null
                    user = result?.userProfile
                    name!!.text = user?.name
                    username!!.text = '@'.toString() + user?.username
                    val d = ColorDrawable(-0x7f7f80)
                    if (user?.photoUrl != null) ViewImageLoader.load(
                        photo,
                        d,
                        user?.photoUrl
                    ) else photo!!.setImageDrawable(d)
                    followsYou!!.visibility = if (user!!.followsMe) View.VISIBLE else View.GONE
                    followers!!.text =
                        resources.getQuantityString(R.plurals.followers, user!!.numFollowers, user?.numFollowers)
                    following!!.text =
                        resources.getQuantityString(R.plurals.following, user!!.numFollowing, user?.numFollowing)
                    bio!!.text = user?.bio
                    if (TextUtils.isEmpty(user?.bio) && self) bio!!.setText(R.string.update_bio)
                    if (self) followBtn!!.visibility =
                        View.GONE else followBtn!!.setText(if (user!!.isFollowed) R.string.following else R.string.follow)
                    if (user?.twitter == null && user?.instagram == null) {
                        socialButtons!!.visibility = View.GONE
                    } else {
                        socialButtons!!.visibility = View.VISIBLE
                        twitter!!.visibility = if (user?.twitter == null) View.GONE else View.VISIBLE
                        instagram!!.visibility = if (user?.instagram == null) View.GONE else View.VISIBLE
                        if (user!!.twitter != null) twitter!!.text = user?.twitter
                        if (user!!.instagram != null) instagram!!.text = user?.instagram
                    }
                    var joined = getString(R.string.joined_date, DateFormat.getDateInstance().format(user!!.timeCreated))
                    if (user?.invitedByUserProfile != null) {
                        val d2 = ColorDrawable(-0x7f7f80)
                        joined += """
                        
                        ${getString(R.string.invited_by, user!!.invitedByUserProfile!!.name)}
                        """.trimIndent()
                        if (user!!.invitedByUserProfile!!.photoUrl != null) ViewImageLoader.load(
                            inviterPhoto,
                            d2,
                            user!!.invitedByUserProfile!!.photoUrl
                        ) else inviterPhoto!!.setImageDrawable(d2)
                    } else {
                        inviterPhoto!!.visibility = View.GONE
                    }
                    inviteInfo!!.text = joined
                    dataLoaded()
                }
            })
            .exec()
    }

    override fun onRefresh() {
        loadData()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        toolbar.elevation = 0f
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        toolbar.elevation = 0f
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        menu.add(R.string.log_out)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (instance != null) {
            instance!!.leaveChannel()
        }
        ClubhouseSession.userToken = null
        ClubhouseSession.userID = ClubhouseSession.userToken
        ClubhouseSession.write()
        Nav.goClearingStack(activity, LoginFragment::class.java, null)
        return true
    }

    private fun onFollowClick(v: View) {
        if (user!!.isFollowed) {
            AlertDialog.Builder(activity)
                .setMessage(getString(R.string.confirm_unfollow, user!!.name))
                .setPositiveButton(R.string.yes) { dialogInterface, i ->
                    Unfollow(user!!.userId)
                        .wrapProgress(activity)
                        .setCallback(object : Callback<BaseResponse?> {
                            override fun onSuccess(result: BaseResponse?) {
                                user!!.notificationType = 0
                                followBtn!!.setText(R.string.follow)
                            }

                            override fun onError(error: ErrorResponse) {
                                error.showToast(activity)
                            }
                        })
                        .exec()
                }
                .setNegativeButton(R.string.no, null)
                .show()
        } else {
            Follow(user!!.userId)
                .wrapProgress(activity)
                .setCallback(object : Callback<BaseResponse?> {
                    override fun onSuccess(result: BaseResponse?) {
                        user!!.notificationType = 2
                        followBtn!!.setText(R.string.following)
                    }

                    override fun onError(error: ErrorResponse) {
                        error.showToast(activity)
                    }
                })
                .exec()
        }
    }

    private fun onInstagramClick(v: View) {
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://instagram.com/" + user!!.instagram)))
    }

    private fun onTwitterClick(v: View) {
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://twitter.com/" + user!!.twitter)))
    }

    private fun onFollowersClick(v: View) {
        val args = Bundle()
        args.putInt("id", user!!.userId)
        Nav.go(activity, FollowersFragment::class.java, args)
    }

    private fun onFollowingClick(v: View) {
        val args = Bundle()
        args.putInt("id", user!!.userId)
        Nav.go(activity, FollowingFragment::class.java, args)
    }

    private fun onInviterClick(v: View) {
        if (user!!.invitedByUserProfile == null) return
        val args = Bundle()
        args.putInt("id", user!!.invitedByUserProfile!!.userId)
        Nav.go(activity, ProfileFragment::class.java, args)
    }

    private fun onBioClick(v: View) {
        val edit = EditText(activity)
        edit.inputType = InputType.TYPE_TEXT_FLAG_CAP_SENTENCES or InputType.TYPE_TEXT_FLAG_MULTI_LINE
        edit.isSingleLine = false
        edit.minLines = 3
        edit.maxLines = 6
        edit.gravity = Gravity.TOP
        edit.setText(user!!.bio)
        AlertDialog.Builder(activity)
            .setTitle(R.string.update_bio)
            .setView(edit)
            .setPositiveButton(R.string.save) { dialogInterface, i ->
                val newBio = edit.text.toString()
                UpdateBio(newBio)
                    .wrapProgress(activity)
                    .setCallback(object : Callback<BaseResponse?> {
                        override fun onSuccess(result: BaseResponse?) {
                            user!!.bio = newBio
                            if (TextUtils.isEmpty(newBio)) bio!!.setText(R.string.update_bio) else bio!!.text = newBio
                        }

                        override fun onError(error: ErrorResponse) {
                            error.showToast(activity)
                        }
                    })
                    .exec()
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }
}