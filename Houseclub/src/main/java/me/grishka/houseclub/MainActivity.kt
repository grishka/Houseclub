package me.grishka.houseclub

import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import me.grishka.appkit.FragmentStackActivity
import me.grishka.appkit.api.Callback
import me.grishka.appkit.api.ErrorResponse
import me.grishka.houseclub.MainActivity
import me.grishka.houseclub.api.ClubhouseSession
import me.grishka.houseclub.api.methods.CheckWaitlistStatus
import me.grishka.houseclub.api.methods.GetChannel
import me.grishka.houseclub.api.methods.JoinChannel
import me.grishka.houseclub.api.model.Channel
import me.grishka.houseclub.fragments.HomeFragment
import me.grishka.houseclub.fragments.InChannelFragment
import me.grishka.houseclub.fragments.LoginFragment
import me.grishka.houseclub.fragments.RegisterFragment
import me.grishka.houseclub.fragments.WaitlistedFragment

class MainActivity : FragmentStackActivity() {
    private var channelToJoin: Channel? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val prefs = getPreferences(MODE_PRIVATE)
        if (!prefs.getBoolean("warningShown", false)) {
            AlertDialog.Builder(this)
                .setTitle(R.string.warning)
                .setMessage(R.string.warning_text)
                .setPositiveButton(R.string.i_understand, null)
                .setCancelable(false)
                .show()
            prefs.edit().putBoolean("warningShown", true).apply()
        }
        if (ClubhouseSession.isLoggedIn) {
            showFragment(if (ClubhouseSession.isWaitlisted) WaitlistedFragment() else HomeFragment())
            if (ClubhouseSession.isWaitlisted) {
                CheckWaitlistStatus()
                    .setCallback(object : Callback<CheckWaitlistStatus.Response?> {
                        override fun onSuccess(result: CheckWaitlistStatus.Response?) {
                            if (!result!!.isWaitlisted) {
                                ClubhouseSession.isWaitlisted = false
                                ClubhouseSession.write()
                                if (result.isOnboarding) showFragmentClearingBackStack(RegisterFragment()) else showFragmentClearingBackStack(
                                    HomeFragment()
                                )
                                //									if(Intent.ACTION_VIEW.equals(getIntent().getAction())){
                                //										joinChannelFromIntent();
                                //									}
                            }
                        }

                        override fun onError(error: ErrorResponse) {}
                    })
                    .exec()
            } else {
                if (Intent.ACTION_VIEW == intent.action) {
                    joinChannelFromIntent()
                }
            }
        } else {
            showFragment(LoginFragment())
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        if (Intent.ACTION_VIEW == intent.action) {
            joinChannelFromIntent()
        } else if (intent.hasExtra("openCurrentChannel")) {
            if (VoiceService.instance != null) {
                val extras = Bundle()
                extras.putBoolean("_can_go_back", true)
                val fragment = InChannelFragment()
                fragment.arguments = extras
                showFragment(fragment)
            }
        }
    }

    private fun joinChannelFromIntent() {
        val data = intent.data
        val id = data?.lastPathSegment.orEmpty()
        GetChannel(id)
            .wrapProgress(this)
            .setCallback(object : Callback<Channel> {
                override fun onSuccess(result: Channel) {
                    AlertDialog.Builder(this@MainActivity)
                        .setTitle(R.string.join_this_room)
                        .setMessage(result.topic)
                        .setPositiveButton(R.string.join) { _, _ -> joinChannel(result) }
                        .setNegativeButton(R.string.cancel, null)
                        .show()
                }

                override fun onError(error: ErrorResponse) {
                    error.showToast(this@MainActivity)
                }
            })
            .exec()
    }

    fun joinChannel(chan: Channel) {
        if (VoiceService.instance != null) {
            val current = VoiceService.instance!!.channel
            if (current?.channel == chan.channel) {
                val extras = Bundle()
                extras.putBoolean("_can_go_back", true)
                val fragment = InChannelFragment()
                fragment.arguments = extras
                showFragment(fragment)
                return
            }
            VoiceService.instance?.leaveChannel()
        }
        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
            JoinChannel(chan.channel!!)
                .wrapProgress(this)
                .setCallback(object : Callback<Channel?> {
                    override fun onSuccess(result: Channel?) {
                        val intent = Intent(this@MainActivity, VoiceService::class.java)
                        intent.putExtra("channel", result)
                        if (Build.VERSION.SDK_INT >= 26) startForegroundService(intent) else startService(intent)
                        val extras = Bundle()
                        extras.putBoolean("_can_go_back", true)
                        val fragment = InChannelFragment()
                        fragment.arguments = extras
                        showFragment(fragment)
                    }

                    override fun onError(error: ErrorResponse) {
                        error.showToast(this@MainActivity)
                    }
                })
                .exec()
        } else {
            channelToJoin = chan
            requestPermissions(arrayOf(Manifest.permission.RECORD_AUDIO), PERMISSION_RESULT)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (requestCode == PERMISSION_RESULT && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (channelToJoin != null) {
                joinChannel(channelToJoin!!)
            }
        }
        channelToJoin = null
    }

    companion object {
        private const val PERMISSION_RESULT = 270
    }
}