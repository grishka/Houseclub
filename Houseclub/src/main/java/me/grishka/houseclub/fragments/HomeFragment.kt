package me.grishka.houseclub.fragments

import android.app.Activity
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.Outline
import android.graphics.Rect
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.ViewOutlineProvider
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ItemDecoration
import me.grishka.appkit.Nav
import me.grishka.appkit.api.SimpleCallback
import me.grishka.appkit.fragments.BaseRecyclerFragment
import me.grishka.appkit.imageloader.ImageLoaderRecyclerAdapter
import me.grishka.appkit.imageloader.ImageLoaderViewHolder
import me.grishka.appkit.utils.BindableViewHolder
import me.grishka.appkit.utils.V
import me.grishka.houseclub.MainActivity
import me.grishka.houseclub.R
import me.grishka.houseclub.api.ClubhouseSession
import me.grishka.houseclub.api.methods.GetChannels
import me.grishka.houseclub.api.model.Channel
import me.grishka.houseclub.api.model.ChannelUser
import java.util.stream.Collectors

class HomeFragment : BaseRecyclerFragment<Channel?>(20) {
    private var adapter: ChannelAdapter? = null
    private val roundedCornersOutline: ViewOutlineProvider = object : ViewOutlineProvider() {
        override fun getOutline(view: View, outline: Outline) {
            outline.setRoundRect(0, 0, view.width, view.height, V.dp(8f).toFloat())
        }
    }

    override fun onAttach(activity: Activity) {
        super.onAttach(activity)
        loadData()
        setHasOptionsMenu(true)
    }

    override fun doLoadData(offset: Int, count: Int) {
        currentRequest = GetChannels()
            .setCallback(object : SimpleCallback<GetChannels.Response?>(this) {
                override fun onSuccess(result: GetChannels.Response?) {
                    currentRequest = null
                    onDataLoaded(result!!.channels, false)
                }
            }).exec()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        list.addItemDecoration(object : ItemDecoration() {
            override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
                outRect.top = V.dp(8f)
                outRect.bottom = outRect.top
                outRect.right = V.dp(16f)
                outRect.left = outRect.right
            }
        })
        toolbar.elevation = 0f
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        toolbar.elevation = 0f
    }

    override fun getAdapter(): RecyclerView.Adapter<*> {
        if (adapter == null) {
            adapter = ChannelAdapter()
            adapter!!.setHasStableIds(true)
        }
        return adapter!!
    }

    override fun wantsLightNavigationBar(): Boolean {
        return true
    }

    override fun wantsLightStatusBar(): Boolean {
        return true
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        menu.add("").setIcon(R.drawable.ic_baseline_person_24).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val args = Bundle()
        args.putInt("id", ClubhouseSession.userID!!.toInt())
        Nav.go(activity, ProfileFragment::class.java, args)
        return true
    }

    private inner class ChannelAdapter : RecyclerView.Adapter<ChannelViewHolder>(), ImageLoaderRecyclerAdapter {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChannelViewHolder {
            return ChannelViewHolder()
        }

        override fun onBindViewHolder(holder: ChannelViewHolder, position: Int) {
            holder.bind(data[position])
        }

        override fun getItemCount(): Int {
            return data.size
        }

        override fun getItemId(position: Int): Long {
            return data[position]!!.channelId.toLong()
        }

        override fun getImageCountForItem(position: Int): Int {
            val chan = data[position]!!
            var count = 0
            for (i in 0 until Math.min(2, chan.users!!.size)) {
                if (chan.users!![i]!!.photoUrl != null) count++
            }
            return count
        }

        override fun getImageURL(position: Int, image: Int): String? {
            var image = image
            val chan = data[position]!!
            for (i in 0 until Math.min(2, chan.users!!.size)) {
                if (chan.users!![i]!!.photoUrl != null) {
                    if (image == 0) return chan.users!![i]!!.photoUrl else image--
                }
            }
            return null
        }
    }

    private inner class ChannelViewHolder : BindableViewHolder<Channel>(activity, R.layout.channel_row),
        View.OnClickListener, ImageLoaderViewHolder {
        private val topic: TextView
        private val speakers: TextView
        private val numMembers: TextView
        private val numSpeakers: TextView
        private val pic1: ImageView
        private val pic2: ImageView
        private val placeholder: Drawable = ColorDrawable(-0x7f7f80)
        override fun onBind(item: Channel) {
            topic.text = item.topic
            numMembers.text = "" + item.numAll
            numSpeakers.text = "" + item.numSpeakers
            speakers.text =
                item.users!!.stream().map { user: ChannelUser? -> if (user!!.isSpeaker) user.name + " 💬" else user.name }
                    .collect(Collectors.joining("\n"))
            imgLoader.bindViewHolder(adapter, this, adapterPosition)
        }

        override fun onClick(view: View) {
            (activity as MainActivity).joinChannel(item)
        }

        private fun imgForIndex(index: Int): ImageView {
            return if (index == 0) pic1 else pic2
        }

        override fun setImage(index: Int, bitmap: Bitmap) {
            var index = index
            if (index == 0 && item!!.users!![0]!!.photoUrl == null) index = 1
            imgForIndex(index).setImageBitmap(bitmap)
        }

        override fun clearImage(index: Int) {
            imgForIndex(index).setImageDrawable(placeholder)
        }

        init {
            topic = findViewById(R.id.topic)
            speakers = findViewById(R.id.speakers)
            numSpeakers = findViewById(R.id.num_speakers)
            numMembers = findViewById(R.id.num_members)
            pic1 = findViewById(R.id.pic1)
            pic2 = findViewById(R.id.pic2)
            itemView.outlineProvider = roundedCornersOutline
            itemView.clipToOutline = true
            itemView.elevation = V.dp(2f).toFloat()
            itemView.setOnClickListener(this)
        }
    }
}