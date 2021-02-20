package me.grishka.houseclub

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import com.google.gson.JsonObject
import com.pubnub.api.PNConfiguration
import com.pubnub.api.PubNub
import com.pubnub.api.callbacks.SubscribeCallback
import com.pubnub.api.models.consumer.PNStatus
import com.pubnub.api.models.consumer.objects_api.channel.PNChannelMetadataResult
import com.pubnub.api.models.consumer.objects_api.membership.PNMembershipResult
import com.pubnub.api.models.consumer.objects_api.uuid.PNUUIDMetadataResult
import com.pubnub.api.models.consumer.pubsub.PNMessageResult
import com.pubnub.api.models.consumer.pubsub.PNPresenceEventResult
import com.pubnub.api.models.consumer.pubsub.PNSignalResult
import com.pubnub.api.models.consumer.pubsub.files.PNFileEventResult
import com.pubnub.api.models.consumer.pubsub.message_actions.PNMessageActionResult
import io.agora.rtc.Constants
import io.agora.rtc.IRtcEngineEventHandler
import io.agora.rtc.RtcEngine
import me.grishka.appkit.api.Callback
import me.grishka.appkit.api.ErrorResponse
import me.grishka.houseclub.MainActivity
import me.grishka.houseclub.api.BaseResponse
import me.grishka.houseclub.api.ClubhouseAPIController
import me.grishka.houseclub.api.ClubhouseSession
import me.grishka.houseclub.api.methods.ActivePing
import me.grishka.houseclub.api.methods.AudienceReply
import me.grishka.houseclub.api.methods.JoinChannel
import me.grishka.houseclub.api.methods.LeaveChannel
import me.grishka.houseclub.api.model.Channel
import me.grishka.houseclub.api.model.ChannelUser
import java.util.ArrayList
import java.util.Arrays
import java.util.stream.Collectors

class VoiceService : Service() {
    private var engine: RtcEngine? = null
    var channel: Channel? = null
        private set
    var isMuted = true
        set(muted) {
            field = muted
            engine!!.muteLocalAudioStream(muted)
        }
    private val uiHandler = Handler(Looper.getMainLooper())
    private val pinger: Runnable = object : Runnable {
        override fun run() {
            ActivePing(channel!!.channel!!).exec()
            uiHandler.postDelayed(this, 30000)
        }
    }
    var isHandRaised = false
        private set
    private var pubnub: PubNub? = null
    private val mutedUserIds = ArrayList<Int>()
    var isSelfSpeaker = false
        private set
    var isSelfModerator = false
        private set

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        engine = try {
            RtcEngine.create(baseContext, ClubhouseAPIController.AGORA_KEY, RtcEngineEventHandler())
        } catch (x: Exception) {
            Log.e(TAG, "Error initializing agora", x)
            stopSelf()
            return
        }
        engine?.setChannelProfile(Constants.CHANNEL_PROFILE_COMMUNICATION)
        engine?.setDefaultAudioRoutetoSpeakerphone(true)
        engine?.enableAudioVolumeIndication(500, 3, false)
        engine?.muteLocalAudioStream(true)
        instance = this
    }

    override fun onDestroy() {
        super.onDestroy()
        instance = null
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        if (engine != null) {
            channel = intent.getParcelableExtra("channel")
            updateChannel(channel)
            val nm = getSystemService(NotificationManager::class.java)
            val n = Notification.Builder(this)
                .setSmallIcon(R.drawable.ic_phone_in_talk)
                .setContentTitle(getString(R.string.ongoing_call))
                .setContentText(intent.getStringExtra("topic"))
                .setContentIntent(
                    PendingIntent.getActivity(
                        this,
                        1,
                        Intent(
                            this,
                            MainActivity::class.java
                        ).putExtra("openCurrentChannel", true),
                        PendingIntent.FLAG_UPDATE_CURRENT
                    )
                )
            if (Build.VERSION.SDK_INT >= 26) {
                if (nm.getNotificationChannel("ongoing") == null) {
                    val nc = NotificationChannel("ongoing", "Ongoing calls", NotificationManager.IMPORTANCE_LOW)
                    nm.createNotificationChannel(nc)
                }
                n.setChannelId("ongoing")
            }
            startForeground(10, n.build())
            doJoinChannel()
        }
        return START_NOT_STICKY
    }

    private fun doJoinChannel() {
        engine!!.joinChannel(channel!!.token, channel!!.channel, "", ClubhouseSession.userID!!.toInt())
        uiHandler.postDelayed(pinger, 30000)
        for (l in listeners) l.onChannelUpdated(channel)
        val pnConf = PNConfiguration()
        pnConf.subscribeKey = ClubhouseAPIController.PUBNUB_SUB_KEY
        pnConf.publishKey = ClubhouseAPIController.PUBNUB_PUB_KEY
        //pnConf.setUuid(UUID.randomUUID().toString());
        pnConf.origin = "clubhouse.pubnub.com"
        pnConf.uuid = ClubhouseSession.userID
        pnConf.setPresenceTimeoutWithCustomInterval(channel!!.pubnubHeartbeatValue, channel!!.pubnubHeartbeatInterval)
        pnConf.authKey = channel!!.pubnubToken
        pubnub = PubNub(pnConf)
        pubnub!!.addListener(object : SubscribeCallback() {
            override fun status(pubnub: PubNub, pnStatus: PNStatus) {
                Log.d(TAG, "status() called with: pubnub = [$pubnub], pnStatus = [$pnStatus]")
            }

            override fun message(pubnub: PubNub, pnMessageResult: PNMessageResult) {
                Log.d(TAG, "message() called with: pubnub = [$pubnub], pnMessageResult = [$pnMessageResult]")
                val msg = pnMessageResult.message.asJsonObject
                val act = msg["action"].asString
                when (act) {
                    "invite_speaker" -> onInvitedAsSpeaker(msg)
                    "join_channel" -> onUserJoined(msg)
                    "leave_channel" -> onUserLeft(msg)
                }
            }

            override fun presence(pubnub: PubNub, pnPresenceEventResult: PNPresenceEventResult) {
                Log.d(
                    TAG,
                    "presence() called with: pubnub = [$pubnub], pnPresenceEventResult = [$pnPresenceEventResult]"
                )
            }

            override fun signal(pubnub: PubNub, pnSignalResult: PNSignalResult) {
                Log.d(TAG, "signal() called with: pubnub = [$pubnub], pnSignalResult = [$pnSignalResult]")
            }

            override fun uuid(pubnub: PubNub, pnUUIDMetadataResult: PNUUIDMetadataResult) {}
            override fun channel(pubnub: PubNub, pnChannelMetadataResult: PNChannelMetadataResult) {
                Log.d(
                    TAG,
                    "channel() called with: pubnub = [$pubnub], pnChannelMetadataResult = [$pnChannelMetadataResult]"
                )
            }

            override fun membership(pubnub: PubNub, pnMembershipResult: PNMembershipResult) {
                Log.d(TAG, "membership() called with: pubnub = [$pubnub], pnMembershipResult = [$pnMembershipResult]")
            }

            override fun messageAction(pubnub: PubNub, pnMessageActionResult: PNMessageActionResult) {
                Log.d(
                    TAG,
                    "messageAction() called with: pubnub = [$pubnub], pnMessageActionResult = [$pnMessageActionResult]"
                )
            }

            override fun file(pubnub: PubNub, pnFileEventResult: PNFileEventResult) {}
        })
        pubnub!!.subscribe().channels(
            Arrays.asList(
                "users." + ClubhouseSession.userID,
                "channel_user." + channel!!.channel + "." + ClubhouseSession.userID,  //				"channel_speakers."+channel.channel,
                "channel_all." + channel!!.channel
            )
        ).execute()
    }

    fun leaveChannel() {
        engine!!.leaveChannel()
        LeaveChannel(channel!!.channel!!)
            .exec()
        stopSelf()
        uiHandler.removeCallbacks(pinger)
        pubnub!!.unsubscribeAll()
        pubnub!!.destroy()
    }

    fun rejoinChannel() {
        engine!!.leaveChannel()
        LeaveChannel(channel!!.channel!!)
            .setCallback(object : Callback<BaseResponse?> {
                override fun onSuccess(result: BaseResponse?) {
                    JoinChannel(channel!!.channel!!)
                        .setCallback(object : Callback<Channel?> {
                            override fun onSuccess(result: Channel?) {
                                updateChannel(result)
                                doJoinChannel()
                            }

                            override fun onError(error: ErrorResponse) {}
                        })
                        .exec()
                }

                override fun onError(error: ErrorResponse) {}
            })
            .exec()
    }

    fun raiseHand() {
        if (!isHandRaised) {
            isHandRaised = true
            AudienceReply(channel!!.channel!!, true)
                .exec()
        }
    }

    fun unraiseHand() {
        if (isHandRaised) {
            isHandRaised = false
            AudienceReply(channel!!.channel!!, false)
                .exec()
        }
    }

    fun updateChannel(chan: Channel?) {
        channel = chan
        isSelfModerator = false
        isSelfSpeaker = false
        val id = ClubhouseSession.userID!!.toInt()
        for (user in channel!!.users!!) {
            if (user?.userId == id) {
                isSelfModerator = user.isModerator
                isSelfSpeaker = user.isSpeaker
                break
            }
        }
    }

    private fun callAddedListener(l: ChannelEventListener) {
        l.onChannelUpdated(channel)
    }

    private fun onInvitedAsSpeaker(msg: JsonObject) {
        val ch = msg["channel"].asString
        if (ch != channel!!.channel) return
        uiHandler.post {
            for (l in listeners) l.onCanSpeak(
                msg["from_name"].asString, msg["from_user_id"].asInt
            )
        }
    }

    private fun onUserJoined(msg: JsonObject) {
        val ch = msg["channel"].asString
        if (ch != channel!!.channel) return
        val profile = msg.getAsJsonObject("user_profile")
        val user = ClubhouseAPIController.instance!!.gson.fromJson(profile, ChannelUser::class.java)
        uiHandler.post {
            channel!!.users!!.add(user)
            for (l in listeners) l.onUserJoined(user)
        }
    }

    private fun onUserLeft(msg: JsonObject) {
        val ch = msg["channel"].asString
        if (ch != channel!!.channel) return
        val id = msg["user_id"].asInt
        uiHandler.post {
            for (user in channel!!.users!!) {
                if (user?.userId == id) {
                    channel!!.users!!.remove(user)
                    break
                }
            }
            for (l in listeners) l.onUserLeft(id)
        }
    }

    interface ChannelEventListener {
        fun onUserMuteChanged(id: Int, muted: Boolean)
        fun onUserJoined(user: ChannelUser?)
        fun onUserLeft(id: Int)
        fun onCanSpeak(inviterName: String?, inviterID: Int)
        fun onChannelUpdated(channel: Channel?)
        fun onSpeakingUsersChanged(ids: List<Int>?)
    }

    private inner class RtcEngineEventHandler : IRtcEngineEventHandler() {
        override fun onJoinChannelSuccess(channel: String, uid: Int, elapsed: Int) {
            Log.d(TAG, "onJoinChannelSuccess() called with: channel = [$channel], uid = [$uid], elapsed = [$elapsed]")
        }

        override fun onError(err: Int) {
            Log.d(TAG, "onError() called with: err = [$err]")
        }

        override fun onAudioVolumeIndication(speakers: Array<AudioVolumeInfo>, totalVolume: Int) {
            //			Log.d(TAG, "onAudioVolumeIndication() called with: speakers = ["+Arrays.toString(speakers)+"], totalVolume = ["+totalVolume+"]");
            uiHandler.post {
                val selfID = ClubhouseSession.userID!!.toInt()
                val uids = Arrays.stream(speakers).map { s: AudioVolumeInfo -> if (s.uid == 0) selfID else s.uid }
                    .collect(Collectors.toList())
                for (l in listeners) l.onSpeakingUsersChanged(uids)
            }
        }

        override fun onUserMuteAudio(uid: Int, muted: Boolean) {
            //			Log.d(TAG, "onUserMuteAudio() called with: uid = ["+uid+"], muted = ["+muted+"]");
            uiHandler.post {
                for (u in channel!!.users!!) {
                    if (u?.userId == uid) {
                        u.isMuted = muted
                        break
                    }
                }
                for (l in listeners) l.onUserMuteChanged(uid, muted)
            }
        }
    }

    companion object {
        private val listeners = ArrayList<ChannelEventListener>()
        var instance: VoiceService? = null
            private set
        private const val TAG = "VoiceService"
        fun addListener(l: ChannelEventListener) {
            if (!listeners.contains(l)) listeners.add(l)
            if (instance != null) {
                instance!!.callAddedListener(l)
            }
        }

        fun removeListener(l: ChannelEventListener) {
            listeners.remove(l)
        }
    }
}