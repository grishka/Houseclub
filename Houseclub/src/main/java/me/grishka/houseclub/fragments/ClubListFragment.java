package me.grishka.houseclub.fragments;

import android.app.Activity;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import me.grishka.appkit.Nav;
import me.grishka.appkit.fragments.BaseRecyclerFragment;
import me.grishka.appkit.imageloader.ImageLoaderRecyclerAdapter;
import me.grishka.appkit.imageloader.ImageLoaderViewHolder;
import me.grishka.appkit.utils.BindableViewHolder;
import me.grishka.appkit.views.UsableRecyclerView;
import me.grishka.houseclub.R;
import me.grishka.houseclub.api.ClubhouseSession;
import me.grishka.houseclub.api.model.Club;

public abstract class ClubListFragment extends BaseRecyclerFragment<Club>{

	private int selfID = Integer.parseInt(ClubhouseSession.userID);
	private ClubListAdapter adapter;

	public ClubListFragment(){
		super(50);
	}

	@Override
	public void onAttach(Activity activity){
		super.onAttach(activity);
		loadData();
	}

	@Override
	protected RecyclerView.Adapter getAdapter(){
		if(adapter==null){
			adapter=new ClubListAdapter();
		}
		return adapter;
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState){
		super.onViewCreated(view, savedInstanceState);
		getToolbar().setElevation(0);
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig){
		super.onConfigurationChanged(newConfig);
		getToolbar().setElevation(0);
	}

	private class ClubListAdapter extends RecyclerView.Adapter<ClubViewHolder> implements ImageLoaderRecyclerAdapter{

		@NonNull
		@Override
		public ClubViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType){
			return new ClubViewHolder();
		}

		@Override
		public void onBindViewHolder(@NonNull ClubViewHolder holder, int position){
			holder.bind(data.get(position));
		}

		@Override
		public int getItemCount(){
			return data.size();
		}

		@Override
		public int getImageCountForItem(int position) { return data.get(position).photo_url!=null ? 1 : 0;}

		@Override
		public String getImageURL(int position, int image) {return data.get(position).photo_url;}

	}

	private class ClubViewHolder extends BindableViewHolder<Club> implements ImageLoaderViewHolder, UsableRecyclerView.Clickable{

		public TextView name, numFollowers, numMembers;
		public Button followBtn;
		public ImageView photo;
		private Drawable placeholder=new ColorDrawable(getResources().getColor(R.color.grey));

		public ClubViewHolder(){
			super(getActivity(), R.layout.club_list_row);

			name=findViewById(R.id.name);
			numFollowers=findViewById(R.id.followersCount);
			numMembers=findViewById(R.id.membersCount);
			followBtn=findViewById(R.id.follow_btn);
			photo=findViewById(R.id.photo);
		}

		@Override
		public void onBind(Club item){
			name.setText(item.name);
			numMembers.setText(item.num_members + " members");
			numFollowers.setText(item.num_followers + " followers");

			// TODO get_followers/get_following don't return current follow status?
//			if(item.userId==selfID){
				followBtn.setVisibility(View.GONE);
//			}else{
//				followBtn.setVisibility(View.VISIBLE);
//				followBtn.setText(item.isFollowed() ? R.string.following : R.string.follow);
//			}


			if(item.photo_url!=null)
				imgLoader.bindViewHolder(adapter, this, getAdapterPosition());
			else
				photo.setImageDrawable(placeholder);


		}

		@Override
		public void setImage(int index, Bitmap bitmap){
			photo.setImageBitmap(bitmap);
		}

		@Override
		public void clearImage(int index){
			photo.setImageDrawable(placeholder);
		}

		@Override
		public void onClick(){
			Bundle args=new Bundle();
			args.putInt("id", item.club_id);
			Nav.go(getActivity(), ClubFragment.class, args);
		}


	}
}
