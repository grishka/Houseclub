package me.grishka.houseclub.fragments

import android.app.Activity
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import me.grishka.appkit.Nav
import me.grishka.appkit.fragments.BaseRecyclerFragment
import me.grishka.appkit.imageloader.ImageLoaderRecyclerAdapter
import me.grishka.appkit.imageloader.ImageLoaderViewHolder
import me.grishka.appkit.utils.BindableViewHolder
import me.grishka.appkit.views.UsableRecyclerView.Clickable
import me.grishka.houseclub.R
import me.grishka.houseclub.api.ClubhouseSession
import me.grishka.houseclub.api.model.FullUser

abstract class UserListFragment : BaseRecyclerFragment<FullUser?>(50) {
    private val selfID = ClubhouseSession.userID!!.toInt()
    private var adapter: UserListAdapter? = null
    override fun onAttach(activity: Activity) {
        super.onAttach(activity)
        loadData()
    }

    override fun getAdapter(): RecyclerView.Adapter<*>? {
        if (adapter == null) {
            adapter = UserListAdapter()
        }
        return adapter
    }

    private inner class UserListAdapter : RecyclerView.Adapter<UserViewHolder>(), ImageLoaderRecyclerAdapter {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
            return UserViewHolder()
        }

        override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
            holder.bind(data[position])
        }

        override fun getItemCount(): Int {
            return data.size
        }

        override fun getImageCountForItem(position: Int): Int {
            return if (data[position]!!.photoUrl != null) 1 else 0
        }

        override fun getImageURL(position: Int, image: Int): String {
            return data[position]!!.photoUrl!!
        }
    }

    private inner class UserViewHolder : BindableViewHolder<FullUser>(activity, R.layout.user_list_row),
        ImageLoaderViewHolder, Clickable {
        var name: TextView
        var bio: TextView
        var followBtn: Button
        var photo: ImageView
        private val placeholder: Drawable = ColorDrawable(-0x7f7f80)
        override fun onBind(item: FullUser) {
            name.text = item.name
            if (TextUtils.isEmpty(item.bio)) {
                bio.visibility = View.GONE
            } else {
                bio.visibility = View.VISIBLE
                bio.text = item.bio
            }
            // TODO get_followers/get_following don't return current follow status?
            //			if(item.userId==selfID){
            followBtn.visibility = View.GONE
            //			}else{
            //				followBtn.setVisibility(View.VISIBLE);
            //				followBtn.setText(item.isFollowed() ? R.string.following : R.string.follow);
            //			}
            if (item.photoUrl != null) imgLoader.bindViewHolder(
                adapter,
                this,
                adapterPosition
            ) else photo.setImageDrawable(placeholder)
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
            name = findViewById(R.id.name)
            bio = findViewById(R.id.bio)
            followBtn = findViewById(R.id.follow_btn)
            photo = findViewById(R.id.photo)
        }
    }
}