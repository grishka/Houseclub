package me.grishka.houseclub.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import me.grishka.appkit.Nav;
import me.grishka.appkit.api.Callback;
import me.grishka.appkit.api.ErrorResponse;
import me.grishka.appkit.api.SimpleCallback;
import me.grishka.appkit.fragments.BaseRecyclerFragment;
import me.grishka.appkit.imageloader.ImageLoaderRecyclerAdapter;
import me.grishka.appkit.imageloader.ImageLoaderViewHolder;
import me.grishka.appkit.utils.BindableViewHolder;
import me.grishka.appkit.utils.MergeRecyclerAdapter;
import me.grishka.appkit.utils.SingleViewRecyclerAdapter;
import me.grishka.appkit.utils.V;
import me.grishka.appkit.views.UsableRecyclerView;
import me.grishka.houseclub.R;
import me.grishka.houseclub.VoiceService;
import me.grishka.houseclub.api.BaseResponse;
import me.grishka.houseclub.api.ClubhouseSession;
import me.grishka.houseclub.api.methods.AcceptSpeakerInvite;
import me.grishka.houseclub.api.methods.GetChannel;
import me.grishka.houseclub.api.model.Channel;
import me.grishka.houseclub.api.model.ChannelUser;

public class InChannelFragment extends BaseRecyclerFragment<ChannelUser> implements VoiceService.ChannelEventListener{

	private MergeRecyclerAdapter adapter;
	private UserListAdapter speakersAdapter, followedAdapter, othersAdapter;
	private ImageButton muteBtn;
	private Button raiseBtn;
	private Channel channel;
	private ArrayList<ChannelUser> speakers=new ArrayList<>(), followedBySpeakers=new ArrayList<>(), otherUsers=new ArrayList<>();
	private ArrayList<Integer> mutedUsers=new ArrayList<>(), speakingUsers=new ArrayList<>();

	public InChannelFragment(){
		super(10);
		setListLayoutId(R.layout.in_channel);
	}

	@Override
	public void onAttach(Activity activity){
		super.onAttach(activity);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState){
		super.onViewCreated(view, savedInstanceState);
		view.findViewById(R.id.leave).setOnClickListener(this::onLeaveClick);

		raiseBtn=view.findViewById(R.id.raise);
		muteBtn=view.findViewById(R.id.mute);

		raiseBtn.setOnClickListener(this::onRaiseClick);
		muteBtn.setOnClickListener(this::onMuteClick);

		GridLayoutManager lm=new GridLayoutManager(getActivity(), 12);
		lm.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup(){
			@Override
			public int getSpanSize(int position){
				RecyclerView.Adapter a=adapter.getAdapterForPosition(position);
				if(a instanceof UserListAdapter){
					if(((UserListAdapter) a).users==speakers)
						return 4;
					return 3;
				}
				return 12;
			}
		});
		list.setLayoutManager(lm);
		list.setPadding(0, V.dp(16), 0, V.dp(16));
		list.setClipToPadding(false);

		VoiceService.addListener(this);
		getToolbar().setElevation(0);

		VoiceService svc=VoiceService.getInstance();
		if(svc!=null){
			muteBtn.setImageResource(svc.isMuted() ? R.drawable.ic_mic_off : R.drawable.ic_mic);
			onUserMuteChanged(Integer.parseInt(ClubhouseSession.userID), svc.isMuted());
		}
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig){
		super.onConfigurationChanged(newConfig);
		getToolbar().setElevation(0);
	}

	@Override
	public void onDestroyView(){
		super.onDestroyView();
		VoiceService.removeListener(this);
	}

	@Override
	protected void doLoadData(int offset, int count){
//		channel=VoiceService.getInstance().getChannel();
//		setTitle(channel.topic);
//		onDataLoaded(channel.users, false);
		new GetChannel(channel.channel)
				.setCallback(new SimpleCallback<Channel>(this){
					@Override
					public void onSuccess(Channel result){
						VoiceService.getInstance().updateChannel(result);
						onChannelUpdated(result);
					}
				})
				.exec();
	}

	private View makeSectionHeader(@StringRes int text){
		TextView view=(TextView) View.inflate(getActivity(), R.layout.category_header, null);
		view.setText(text);
		return view;
	}

	@Override
	protected RecyclerView.Adapter getAdapter(){
		if(adapter==null){
			adapter=new MergeRecyclerAdapter();
			adapter.addAdapter(speakersAdapter=new UserListAdapter(speakers, View.generateViewId()));
			adapter.addAdapter(new SingleViewRecyclerAdapter(makeSectionHeader(R.string.followed_by_speakers)));
			adapter.addAdapter(followedAdapter=new UserListAdapter(followedBySpeakers, View.generateViewId()));
			adapter.addAdapter(new SingleViewRecyclerAdapter(makeSectionHeader(R.string.others_in_room)));
			adapter.addAdapter(othersAdapter=new UserListAdapter(otherUsers, View.generateViewId()));
		}
		return adapter;
	}

	private void onLeaveClick(View v){
		VoiceService.getInstance().leaveChannel();
		Nav.finish(this);
	}

	private void onRaiseClick(View v){
		VoiceService svc=VoiceService.getInstance();
		if(svc.isHandRaised())
			svc.unraiseHand();
		else
			svc.raiseHand();
	}

	private void onMuteClick(View v){
		VoiceService svc=VoiceService.getInstance();
		svc.setMuted(!svc.isMuted());
		muteBtn.setImageResource(svc.isMuted() ? R.drawable.ic_mic_off : R.drawable.ic_mic);
		onUserMuteChanged(Integer.parseInt(ClubhouseSession.userID), svc.isMuted());
	}

	@Override
	public void onUserMuteChanged(int id, boolean muted){
		int i=0;
		if(muted){
			if(!mutedUsers.contains(id))
				mutedUsers.add(id);
		}else{
			mutedUsers.remove((Integer)id);
		}
		for(ChannelUser user:speakers){
			if(user.userId==id){
				user.isMuted=muted;
				RecyclerView.ViewHolder h=list.findViewHolderForAdapterPosition(i);
				if(h instanceof UserViewHolder){
					((UserViewHolder) h).muted.setVisibility(muted ? View.VISIBLE : View.INVISIBLE);
				}
			}
			i++;
		}
	}

	@Override
	public void onUserJoined(ChannelUser user){
		if(user.isSpeaker){
			speakers.add(user);
			speakersAdapter.notifyItemInserted(speakers.size()-1);
		}else if(user.isFollowedBySpeaker){
			followedBySpeakers.add(user);
			followedAdapter.notifyItemInserted(followedBySpeakers.size()-1);
		}else{
			otherUsers.add(user);
			othersAdapter.notifyItemInserted(otherUsers.size()-1);
		}
	}

	@Override
	public void onUserLeft(int id){
		int i=0;
		for(ChannelUser user:speakers){
			if(user.userId==id){
				speakers.remove(user);
				speakersAdapter.notifyItemRemoved(i);
				return;
			}
			i++;
		}
		i=0;
		for(ChannelUser user:followedBySpeakers){
			if(user.userId==id){
				followedBySpeakers.remove(user);
				followedAdapter.notifyItemRemoved(i);
				return;
			}
			i++;
		}
		i=0;
		for(ChannelUser user:otherUsers){
			if(user.userId==id){
				otherUsers.remove(user);
				othersAdapter.notifyItemRemoved(i);
				return;
			}
			i++;
		}
	}

	@Override
	public void onCanSpeak(String inviterName, int inviterID){
		new AlertDialog.Builder(getActivity())
				.setMessage(getString(R.string.confirm_join_as_speaker, inviterName))
				.setPositiveButton(R.string.join, new DialogInterface.OnClickListener(){
					@Override
					public void onClick(DialogInterface dialogInterface, int i){
						new AcceptSpeakerInvite(channel.channel, inviterID)
								.wrapProgress(getActivity())
								.setCallback(new Callback<BaseResponse>(){
									@Override
									public void onSuccess(BaseResponse result){
										VoiceService.getInstance().rejoinChannel();
									}

									@Override
									public void onError(ErrorResponse error){
										error.showToast(getActivity());
									}
								})
								.exec();
					}
				})
				.setNegativeButton(R.string.cancel, null)
				.show();
	}

	@Override
	public void onChannelUpdated(Channel channel){
		this.channel=channel;
		setTitle(channel.topic);
		speakers.clear();
		followedBySpeakers.clear();
		otherUsers.clear();
		for(ChannelUser user:channel.users){
			if(user.isMuted && !mutedUsers.contains(user.userId))
				mutedUsers.add(user.userId);
			if(user.isSpeaker)
				speakers.add(user);
			else if(user.isFollowedBySpeaker)
				followedBySpeakers.add(user);
			else
				otherUsers.add(user);
		}
		onDataLoaded(channel.users, false);

		VoiceService svc=VoiceService.getInstance();
		raiseBtn.setEnabled(channel.isHandraiseEnabled);
		raiseBtn.setVisibility(svc.isSelfSpeaker() ? View.GONE : View.VISIBLE);
		muteBtn.setVisibility(svc.isSelfSpeaker() ? View.VISIBLE : View.GONE);
		if(svc.isSelfSpeaker()){
			onUserMuteChanged(Integer.parseInt(ClubhouseSession.userID), svc.isMuted());
		}
	}

	@Override
	public void onSpeakingUsersChanged(List<Integer> ids){
		speakingUsers.clear();
		speakingUsers.addAll(ids);

		int i=0;
		for(ChannelUser user:speakers){
			RecyclerView.ViewHolder h=list.findViewHolderForAdapterPosition(i);
			if(h instanceof UserViewHolder){
				((UserViewHolder) h).speakerBorder.setAlpha(speakingUsers.contains(user.userId) ? 1 : 0);
			}
			i++;
		}
	}

	@Override
	public void onChannelEnded(){
		Nav.finish(this);
	}

	@Override
	public void onSelfLeft() { }

	private class UserListAdapter extends RecyclerView.Adapter<UserViewHolder> implements ImageLoaderRecyclerAdapter{

		private List<ChannelUser> users;
		private int type;

		public UserListAdapter(List<ChannelUser> users, int type){
			this.users=users;
			this.type=type;
		}

		@NonNull
		@Override
		public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType){
			return new UserViewHolder(users==speakers);
		}

		@Override
		public void onBindViewHolder(@NonNull UserViewHolder holder, int position){
			holder.bind(users.get(position));
		}

		@Override
		public int getItemViewType(int position){
			return type;
		}

		@Override
		public int getItemCount(){
			return users.size();
		}

		@Override
		public int getImageCountForItem(int position){
			return users.get(position).photoUrl!=null ? 1 : 0;
		}

		@Override
		public String getImageURL(int position, int image){
			return users.get(position).photoUrl;
		}
	}

	private class UserViewHolder extends BindableViewHolder<ChannelUser> implements ImageLoaderViewHolder, UsableRecyclerView.Clickable{

		private ImageView photo, muted;
		private TextView name;
		private View speakerBorder;
		private Drawable placeholder=new ColorDrawable(getResources().getColor(R.color.grey));

		public UserViewHolder(boolean large){
			super(getActivity(), R.layout.channel_user_cell, list);

			photo=findViewById(R.id.photo);
			name=findViewById(R.id.name);
			muted=findViewById(R.id.muted);
			speakerBorder=findViewById(R.id.speaker_border);

			ViewGroup.LayoutParams lp=photo.getLayoutParams();
			lp.width=lp.height=V.dp(large ? 72 : 48);
			muted.setVisibility(View.INVISIBLE);
			if(!large)
				speakerBorder.setVisibility(View.GONE);
			else
				speakerBorder.setAlpha(0);
		}

		@Override
		public void onBind(ChannelUser item){
			if(item.isModerator)
				name.setText("✱ "+item.firstName);
			else
				name.setText(item.firstName);
			muted.setVisibility(mutedUsers.contains(item.userId) ? View.VISIBLE : View.INVISIBLE);
			speakerBorder.setAlpha(speakingUsers.contains(item.userId) ? 1 : 0);

			if(item.photoUrl==null)
				photo.setImageDrawable(placeholder);
			else
				imgLoader.bindViewHolder(adapter, this, getAdapterPosition());
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
			args.putInt("id", item.userId);
			Nav.go(getActivity(), ProfileFragment.class, args);
		}
	}
}
