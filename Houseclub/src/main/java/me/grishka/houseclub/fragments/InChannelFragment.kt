package me.grishka.houseclub.fragments

import android.app.Activity
import android.app.AlertDialog
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.StringRes
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.GridLayoutManager.SpanSizeLookup
import androidx.recyclerview.widget.RecyclerView
import me.grishka.appkit.Nav
import me.grishka.appkit.api.Callback
import me.grishka.appkit.api.ErrorResponse
import me.grishka.appkit.api.SimpleCallback
import me.grishka.appkit.fragments.BaseRecyclerFragment
import me.grishka.appkit.imageloader.ImageLoaderRecyclerAdapter
import me.grishka.appkit.imageloader.ImageLoaderViewHolder
import me.grishka.appkit.utils.BindableViewHolder
import me.grishka.appkit.utils.MergeRecyclerAdapter
import me.grishka.appkit.utils.SingleViewRecyclerAdapter
import me.grishka.appkit.utils.V
import me.grishka.appkit.views.UsableRecyclerView.Clickable
import me.grishka.houseclub.R
import me.grishka.houseclub.VoiceService.ChannelEventListener
import me.grishka.houseclub.VoiceService.Companion.addListener
import me.grishka.houseclub.VoiceService.Companion.instance
import me.grishka.houseclub.VoiceService.Companion.removeListener
import me.grishka.houseclub.api.BaseResponse
import me.grishka.houseclub.api.ClubhouseSession
import me.grishka.houseclub.api.methods.AcceptSpeakerInvite
import me.grishka.houseclub.api.methods.GetChannel
import me.grishka.houseclub.api.model.Channel
import me.grishka.houseclub.api.model.ChannelUser
import java.util.ArrayList

class InChannelFragment : BaseRecyclerFragment<ChannelUser?>(10), ChannelEventListener {
    private var adapter: MergeRecyclerAdapter? = null
    private var speakersAdapter: UserListAdapter? = null
    private var followedAdapter: UserListAdapter? = null
    private var othersAdapter: UserListAdapter? = null
    private var muteBtn: ImageButton? = null
    private var raiseBtn: Button? = null
    private var channel: Channel? = null
    private val speakers = ArrayList<ChannelUser?>()
    private val followedBySpeakers = ArrayList<ChannelUser?>()
    private val otherUsers = ArrayList<ChannelUser?>()
    private val mutedUsers = ArrayList<Int>()
    private val speakingUsers = ArrayList<Int>()
    override fun onAttach(activity: Activity) {
        super.onAttach(activity)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.findViewById<View>(R.id.leave).setOnClickListener { v: View -> onLeaveClick(v) }
        raiseBtn = view.findViewById(R.id.raise)
        muteBtn = view.findViewById(R.id.mute)
        raiseBtn?.setOnClickListener(View.OnClickListener { v: View -> onRaiseClick(v) })
        muteBtn?.setOnClickListener(View.OnClickListener { v: View -> onMuteClick(v) })
        val lm = GridLayoutManager(activity, 12)
        lm.spanSizeLookup = object : SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                val a = adapter!!.getAdapterForPosition(position)
                return if (a is UserListAdapter) {
                    if (a.users === speakers) 4 else 3
                } else 12
            }
        }
        list.layoutManager = lm
        list.setPadding(0, V.dp(16f), 0, V.dp(16f))
        list.clipToPadding = false
        addListener(this)
        toolbar.elevation = 0f
        val svc = instance
        if (svc != null) {
            muteBtn?.setImageResource(if (svc.isMuted) R.drawable.ic_mic_off else R.drawable.ic_mic)
            onUserMuteChanged(ClubhouseSession.userID!!.toInt(), svc.isMuted)
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        toolbar.elevation = 0f
    }

    override fun onDestroyView() {
        super.onDestroyView()
        removeListener(this)
    }

    override fun doLoadData(offset: Int, count: Int) {
        //		channel=VoiceService.getInstance().getChannel();
        //		setTitle(channel.topic);
        //		onDataLoaded(channel.users, false);
        GetChannel(channel!!.channel!!)
            .setCallback(object : SimpleCallback<Channel?>(this) {
                override fun onSuccess(result: Channel?) {
                    instance!!.updateChannel(result)
                    onChannelUpdated(result)
                }
            })
            .exec()
    }

    private fun makeSectionHeader(@StringRes text: Int): View {
        val view = View.inflate(activity, R.layout.category_header, null) as TextView
        view.setText(text)
        return view
    }

    override fun getAdapter(): RecyclerView.Adapter<*> {
        if (adapter == null) {
            adapter = MergeRecyclerAdapter()
            adapter!!.addAdapter(UserListAdapter(speakers, View.generateViewId()).also { speakersAdapter = it })
            adapter!!.addAdapter(SingleViewRecyclerAdapter(makeSectionHeader(R.string.followed_by_speakers)))
            adapter!!.addAdapter(UserListAdapter(followedBySpeakers, View.generateViewId()).also {
                followedAdapter = it
            })
            adapter!!.addAdapter(SingleViewRecyclerAdapter(makeSectionHeader(R.string.others_in_room)))
            adapter!!.addAdapter(UserListAdapter(otherUsers, View.generateViewId()).also { othersAdapter = it })
        }
        return adapter!!
    }

    private fun onLeaveClick(v: View) {
        instance!!.leaveChannel()
        Nav.finish(this)
    }

    private fun onRaiseClick(v: View) {
        val svc = instance
        if (svc!!.isHandRaised) svc.unraiseHand() else svc.raiseHand()
    }

    private fun onMuteClick(v: View) {
        val svc = instance
        svc!!.isMuted = !svc.isMuted
        muteBtn!!.setImageResource(if (svc.isMuted) R.drawable.ic_mic_off else R.drawable.ic_mic)
        onUserMuteChanged(ClubhouseSession.userID!!.toInt(), svc.isMuted)
    }

    override fun onUserMuteChanged(id: Int, muted: Boolean) {
        var i = 0
        if (muted) {
            if (!mutedUsers.contains(id)) mutedUsers.add(id)
        } else {
            mutedUsers.remove(id)
        }
        for (user in speakers) {
            if (user!!.userId == id) {
                user.isMuted = muted
                val h = list.findViewHolderForAdapterPosition(i)
                if (h is UserViewHolder) {
                    h.muted.visibility = if (muted) View.VISIBLE else View.INVISIBLE
                }
            }
            i++
        }
    }

    override fun onUserJoined(user: ChannelUser?) {
        if (user!!.isSpeaker) {
            speakers.add(user)
            speakersAdapter!!.notifyItemInserted(speakers.size - 1)
        } else if (user.isFollowedBySpeaker) {
            followedBySpeakers.add(user)
            followedAdapter!!.notifyItemInserted(followedBySpeakers.size - 1)
        } else {
            otherUsers.add(user)
            othersAdapter!!.notifyItemInserted(otherUsers.size - 1)
        }
    }

    override fun onUserLeft(id: Int) {
        var i = 0
        for (user in speakers) {
            if (user!!.userId == id) {
                speakers.remove(user)
                speakersAdapter!!.notifyItemRemoved(i)
                return
            }
            i++
        }
        i = 0
        for (user in followedBySpeakers) {
            if (user!!.userId == id) {
                followedBySpeakers.remove(user)
                followedAdapter!!.notifyItemRemoved(i)
                return
            }
            i++
        }
        i = 0
        for (user in otherUsers) {
            if (user!!.userId == id) {
                otherUsers.remove(user)
                othersAdapter!!.notifyItemRemoved(i)
                return
            }
            i++
        }
    }

    override fun onCanSpeak(inviterName: String?, inviterID: Int) {
        AlertDialog.Builder(activity)
            .setMessage(getString(R.string.confirm_join_as_speaker, inviterName))
            .setPositiveButton(R.string.join) { dialogInterface, i ->
                AcceptSpeakerInvite(channel!!.channel!!, inviterID)
                    .wrapProgress(activity)
                    .setCallback(object : Callback<BaseResponse?> {
                        override fun onSuccess(result: BaseResponse?) {
                            instance!!.rejoinChannel()
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

    override fun onChannelUpdated(channel: Channel?) {
        this.channel = channel
        setTitle(channel!!.topic)
        speakers.clear()
        followedBySpeakers.clear()
        otherUsers.clear()
        for (user in channel.users!!) {
            if (user!!.isMuted && !mutedUsers.contains(user.userId)) mutedUsers.add(user.userId)
            if (user.isSpeaker) speakers.add(user) else if (user.isFollowedBySpeaker) followedBySpeakers.add(user) else otherUsers.add(
                user
            )
        }
        onDataLoaded(channel.users, false)
        val svc = instance
        raiseBtn!!.isEnabled = channel.isHandraiseEnabled
        raiseBtn!!.visibility = if (svc!!.isSelfSpeaker) View.GONE else View.VISIBLE
        muteBtn!!.visibility = if (svc.isSelfSpeaker) View.VISIBLE else View.GONE
    }

    override fun onSpeakingUsersChanged(ids: List<Int>?) {
        speakingUsers.clear()
        speakingUsers.addAll(ids!!)
        var i = 0
        for (user in speakers) {
            val h = list.findViewHolderForAdapterPosition(i)
            if (h is UserViewHolder) {
                h.speakerBorder.setAlpha(
                    if (speakingUsers.contains(
                            user!!.userId
                        )) 1.toFloat() else 0.toFloat()
                )
            }
            i++
        }
    }

    private inner class UserListAdapter(val users: List<ChannelUser?>, private val type: Int) :
        RecyclerView.Adapter<UserViewHolder>(), ImageLoaderRecyclerAdapter {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
            return UserViewHolder(users === speakers)
        }

        override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
            holder.bind(users[position])
        }

        override fun getItemViewType(position: Int): Int {
            return type
        }

        override fun getItemCount(): Int {
            return users.size
        }

        override fun getImageCountForItem(position: Int): Int {
            return if (users[position]!!.photoUrl != null) 1 else 0
        }

        override fun getImageURL(position: Int, image: Int): String {
            return users[position]!!.photoUrl!!
        }
    }

    private inner class UserViewHolder(large: Boolean) :
        BindableViewHolder<ChannelUser>(activity, R.layout.channel_user_cell, list), ImageLoaderViewHolder, Clickable {
        private val photo: ImageView
        val muted: ImageView
        private val name: TextView
        val speakerBorder: View
        private val placeholder: Drawable = ColorDrawable(-0x7f7f80)
        override fun onBind(item: ChannelUser) {
            if (item.isModerator) name.text = "✱ " + item.firstName else name.text = item.firstName
            muted.visibility = if (mutedUsers.contains(item.userId)) View.VISIBLE else View.INVISIBLE
            speakerBorder.setAlpha(if (speakingUsers.contains(item.userId)) 1.toFloat() else 0.toFloat())
            if (item.photoUrl == null) photo.setImageDrawable(placeholder) else imgLoader.bindViewHolder(
                adapter,
                this,
                adapterPosition
            )
        }

        override fun setImage(index: Int, bitmap: Bitmap) {
            photo.setImageBitmap(bitmap)
        }

        override fun clearImage(index: Int) {
            photo.setImageDrawable(placeholder)
        }

        override fun onClick() {
            val args = Bundle()
            args.putInt("id", item.userId)
            Nav.go(activity, ProfileFragment::class.java, args)
        }

        init {
            photo = findViewById(R.id.photo)
            name = findViewById(R.id.name)
            muted = findViewById(R.id.muted)
            speakerBorder = findViewById(R.id.speaker_border)
            val lp = photo.layoutParams
            lp.height = V.dp(if (large) 72.toFloat() else 48.toFloat())
            lp.width = lp.height
            muted.visibility = View.INVISIBLE
            if (!large) speakerBorder.visibility = View.GONE else speakerBorder.alpha = 0f
        }
    }

    init {
        setListLayoutId(R.layout.in_channel)
    }
}