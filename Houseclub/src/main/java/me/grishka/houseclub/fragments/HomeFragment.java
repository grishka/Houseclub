package me.grishka.houseclub.fragments;

import android.app.Activity;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Outline;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;
import java.util.stream.Collectors;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import me.grishka.appkit.Nav;
import me.grishka.appkit.api.SimpleCallback;
import me.grishka.appkit.fragments.BaseRecyclerFragment;
import me.grishka.appkit.imageloader.ImageLoaderRecyclerAdapter;
import me.grishka.appkit.imageloader.ImageLoaderViewHolder;
import me.grishka.appkit.utils.BindableViewHolder;
import me.grishka.appkit.utils.V;
import me.grishka.houseclub.DataProvider;
import me.grishka.houseclub.MainActivity;
import me.grishka.houseclub.R;
import me.grishka.houseclub.VoiceService;
import me.grishka.houseclub.api.ClubhouseSession;
import me.grishka.houseclub.api.methods.GetChannels;
import me.grishka.houseclub.api.model.Channel;
import me.grishka.houseclub.api.model.ChannelUser;

public class HomeFragment extends BaseRecyclerFragment<Channel>{

	private ChannelAdapter adapter;
	private View returnView;

	private ViewOutlineProvider roundedCornersOutline=new ViewOutlineProvider(){
		@Override
		public void getOutline(View view, Outline outline){
			outline.setRoundRect(0, 0, view.getWidth(), view.getHeight(), V.dp(8));
		}
	};

	public HomeFragment(){
		super(20);
	}

	@Override
	public void onAttach(Activity activity){
		super.onAttach(activity);
		loadData();
		setHasOptionsMenu(true);
	}

	@Override
	protected void doLoadData(int offset, int count){
		currentRequest=new GetChannels()
				.setCallback(new SimpleCallback<GetChannels.Response>(this){
					@Override
					public void onSuccess(GetChannels.Response result){
						currentRequest=null;
						onDataLoaded(result.channels, false);
					}
				}).exec();
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState){
		super.onViewCreated(view, savedInstanceState);
		list.addItemDecoration(new RecyclerView.ItemDecoration(){
			@Override
			public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state){
				outRect.bottom=outRect.top=V.dp(8);
				outRect.left=outRect.right=V.dp(16);
			}
		});
		getToolbar().setElevation(0);

		// add Return to "channel" bar to bottom of toolbar
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        View returnBar = inflater.inflate(R.layout.return_row_bar, null);
        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

		((ViewGroup) getView()).addView(returnBar, 1, layoutParams);
		returnBar.setOnClickListener((it) -> {
            Channel channel = DataProvider.getCachedChannel();
            if (channel != null)
                ((MainActivity) getActivity()).joinChannel(channel);
		});
		returnView = getView().findViewById(R.id.return_container);
        VoiceService.addListener(channelEventListener);
	}

	private final VoiceService.ChannelEventListener channelEventListener = new VoiceService.ChannelEventListener() {
		@Override
		public void onUserMuteChanged(int id, boolean muted) {
		}

		@Override
		public void onUserJoined(ChannelUser user) {
		}

		@Override
		public void onUserLeft(int id) {
		}

		@Override
		public void onCanSpeak(String inviterName, int inviterID) {
		}

		@Override
		public void onChannelUpdated(Channel channel) {
			checkReturnBar();
		}

		@Override
		public void onSpeakingUsersChanged(List<Integer> ids) {
		}

		@Override
		public void onChannelEnded() {
			hideReturnBar();
		}

		@Override
		public void onSelfLeft() {
			hideReturnBar();
		}

	};

    private void hideReturnBar() {
		if (returnView != null) {
    		returnView.setVisibility(View.GONE);
    	}
    }

    private void checkReturnBar() {
        try {
            Channel channel = DataProvider.getCachedChannel();
            if (returnView != null) {
                if (channel != null) {
                    TextView title = returnView.findViewById(R.id.return_title);
                    if (title != null) {
			String channelNameShortened;
			if (channel.topic == null) channelNameShortened = "the channel";
			else {
				channelNameShortened = channel.topic.substring(0, Math.min(channel.topic.length(), 16));
				if (channelNameShortened.length() < channel.topic.length())
					channelNameShortened += "...";
			}
                        title.setText(getString(R.string.return_to_channel, channelNameShortened));
                    }
                    returnView.setVisibility(View.VISIBLE);
                } else returnView.setVisibility(View.GONE);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

	@Override
	public void onConfigurationChanged(Configuration newConfig){
		super.onConfigurationChanged(newConfig);
		getToolbar().setElevation(0);
	}

	@Override
	protected RecyclerView.Adapter getAdapter(){
		if(adapter==null){
			adapter=new ChannelAdapter();
			adapter.setHasStableIds(true);
		}
		return adapter;
	}
	@Override
	public boolean wantsLightNavigationBar(){
		return true;
	}

	@Override
	public boolean wantsLightStatusBar(){
		return true;
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater){
		menu.add(0,0,0,"").setIcon(R.drawable.ic_notifications).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
		menu.add(0,1,0,"").setIcon(R.drawable.ic_baseline_person_24).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item){
		Bundle args=new Bundle();
		args.putInt("id", Integer.parseInt(ClubhouseSession.userID));
		if(item.getItemId()==0) {
			Nav.go(getActivity(), NotificationListFragment.class, args);
		} else if(item.getItemId()==1){
			Nav.go(getActivity(), ProfileFragment.class, args);
		}
		return true;
	}

	private class ChannelAdapter extends RecyclerView.Adapter<ChannelViewHolder> implements ImageLoaderRecyclerAdapter{

		@NonNull
		@Override
		public ChannelViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType){
			return new ChannelViewHolder();
		}

		@Override
		public void onBindViewHolder(@NonNull ChannelViewHolder holder, int position){
			holder.bind(data.get(position));
		}

		@Override
		public int getItemCount(){
			return data.size();
		}

		@Override
		public long getItemId(int position){
			return data.get(position).channelId;
		}

		@Override
		public int getImageCountForItem(int position){
			Channel chan=data.get(position);
			int count=0;
			for(int i=0;i<Math.min(2, chan.users.size());i++){
				if(chan.users.get(i).photoUrl!=null)
					count++;
			}
			return count;
		}

		@Override
		public String getImageURL(int position, int image){
			Channel chan=data.get(position);
			for(int i=0;i<Math.min(2, chan.users.size());i++){
				if(chan.users.get(i).photoUrl!=null){
					if(image==0)
						return chan.users.get(i).photoUrl;
					else
						image--;
				}
			}
			return null;
		}
	}

	private class ChannelViewHolder extends BindableViewHolder<Channel> implements View.OnClickListener, ImageLoaderViewHolder{

		private TextView topic, speakers, numMembers, numSpeakers;
		private ImageView pic1, pic2;
		private Drawable placeholder=new ColorDrawable(getResources().getColor(R.color.grey));

		public ChannelViewHolder(){
			super(getActivity(), R.layout.channel_row);
			topic=findViewById(R.id.topic);
			speakers=findViewById(R.id.speakers);
			numSpeakers=findViewById(R.id.num_speakers);
			numMembers=findViewById(R.id.num_members);
			pic1=findViewById(R.id.pic1);
			pic2=findViewById(R.id.pic2);

			itemView.setOutlineProvider(roundedCornersOutline);
			itemView.setClipToOutline(true);
			itemView.setOnClickListener(this);
		}

		@Override
		public void onBind(Channel item){
			topic.setText(item.topic);
			numMembers.setText(""+item.numAll);
			numSpeakers.setText(""+item.numSpeakers);
			speakers.setText(item.users.stream().map(user->user.isSpeaker ? (user.name+" 💬") : user.name).collect(Collectors.joining("\n")));

			imgLoader.bindViewHolder(adapter, this, getAdapterPosition());
		}

		@Override
		public void onClick(View view){
			((MainActivity)getActivity()).joinChannel(item);
		}

		private ImageView imgForIndex(int index){
			if(index==0)
				return pic1;
			return pic2;
		}

		@Override
		public void setImage(int index, Bitmap bitmap){
			if(index==0 && item.users.get(0).photoUrl==null)
				index=1;
			imgForIndex(index).setImageBitmap(bitmap);
		}

		@Override
		public void clearImage(int index){
			imgForIndex(index).setImageDrawable(placeholder);
		}
	}
}
