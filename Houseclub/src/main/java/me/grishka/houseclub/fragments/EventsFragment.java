package me.grishka.houseclub.fragments;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Outline;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.stream.Collectors;

import me.grishka.appkit.api.Callback;
import me.grishka.appkit.api.ErrorResponse;
import me.grishka.appkit.api.SimpleCallback;
import me.grishka.appkit.fragments.BaseRecyclerFragment;
import me.grishka.appkit.imageloader.ImageLoaderRecyclerAdapter;
import me.grishka.appkit.imageloader.ImageLoaderViewHolder;
import me.grishka.appkit.utils.BindableViewHolder;
import me.grishka.appkit.utils.V;
import me.grishka.houseclub.MainActivity;
import me.grishka.houseclub.R;
import me.grishka.houseclub.VoiceService;
import me.grishka.houseclub.api.methods.GetChannel;
import me.grishka.houseclub.api.methods.GetEvent;
import me.grishka.houseclub.api.methods.GetEvents;
import me.grishka.houseclub.api.methods.JoinChannel;
import me.grishka.houseclub.api.model.Channel;
import me.grishka.houseclub.api.model.Event;

public class EventsFragment extends BaseRecyclerFragment<Event>{

	private EventsAdapter adapter;
	private ViewOutlineProvider roundedCornersOutline =new ViewOutlineProvider(){
		@Override
		public void getOutline(View view, Outline outline){
			outline.setRoundRect(0, 0, view.getWidth(), view.getHeight(), V.dp(8));
		}
	};

	public EventsFragment(){
		super(20);
	}

	@Override
	public void onAttach(Activity activity){
		super.onAttach(activity);
		setTitle(R.string.event_title);
		loadData();
		setHasOptionsMenu(true);
	}

	@Override
	protected void doLoadData(int offset, int count){
		currentRequest=new GetEvents()
				.setCallback(new SimpleCallback<GetEvents.Response>(this){
					@Override
					public void onSuccess(GetEvents.Response result){
						currentRequest=null;
						onDataLoaded(result.events, false);
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
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig){
		super.onConfigurationChanged(newConfig);
		getToolbar().setElevation(0);
	}

	@Override
	protected RecyclerView.Adapter getAdapter(){
		if(adapter==null){
			adapter=new EventsAdapter();
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

	private class EventsAdapter extends RecyclerView.Adapter<EventViewHolder> implements ImageLoaderRecyclerAdapter{

		@NonNull
		@Override
		public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType){
			return new EventViewHolder();
		}

		@Override
		public void onBindViewHolder(@NonNull EventViewHolder holder, int position){
			holder.bind(data.get(position));
		}

		@Override
		public int getItemCount(){
			return data.size();
		}

		@Override
		public long getItemId(int position){
			return data.get(position).eventId;
		}

		@Override
		public int getImageCountForItem(int position){
			Event eve= data.get(position);
			int count=0;
			for(int i=0;i<Math.min(2, eve.hosts.size());i++){
				if(eve.hosts.get(i).photoUrl!=null)
					count++;
			}
			return count;
		}

		@Override
		public String getImageURL(int position, int image){
			Event eve=data.get(position);
			for(int i=0;i<Math.min(2, eve.hosts.size());i++){
				if(eve.hosts.get(i).photoUrl!=null){
					if(image==0)
						return eve.hosts.get(i).photoUrl;
					else
						image--;
				}
			}
			return null;
		}
	}

	private class EventViewHolder extends BindableViewHolder<Event> implements View.OnClickListener, ImageLoaderViewHolder{

		private TextView start_at ,  topic, hosts,description;
		private ImageView pic1, pic2;
		private Drawable placeholder=new ColorDrawable(getResources().getColor(R.color.grey));


		public EventViewHolder() {
			super(getActivity(), R.layout.event_row);
			start_at=findViewById(R.id.event_start_time);
			topic=findViewById(R.id.topic);
			description=findViewById(R.id.description);
			hosts=findViewById(R.id.hosts);
			pic1=findViewById(R.id.pic1);
			pic2=findViewById(R.id.pic2);

			itemView.setOutlineProvider(roundedCornersOutline);
			itemView.setClipToOutline(true);
			itemView.setElevation(V.dp(2));
			itemView.setOnClickListener(this);

		}



		@Override
		public void onBind(Event item){

			DateFormat dateFormat = new SimpleDateFormat("dd MMMM yyyy hh:mm");
			String strDate = dateFormat.format(item.timeStart);
			start_at.setText(strDate);

			topic.setText(item.name);

			description.setText(item.description);

			hosts.setText(

					item.hosts.stream()
							.map(hosts-> hosts.name)
							.collect(Collectors.joining("\n"))
			);

			imgLoader.bindViewHolder(adapter, this, getAdapterPosition());
		}

		@Override
		public void onClick(View view) {
		}

		private void joinChannelById(String id){
			new GetChannel(id)
					.wrapProgress(getActivity())
					.setCallback(new Callback<Channel>(){
						@Override
						public void onSuccess(final Channel result){
							new AlertDialog.Builder(getActivity())
									.setTitle(R.string.join_this_room)
									.setMessage(result.topic)
									.setPositiveButton(R.string.join, new DialogInterface.OnClickListener(){
										@Override
										public void onClick(DialogInterface dialogInterface, int i){
//											joinChannel(result);
										}
									})
									.setNegativeButton(R.string.cancel, null)
									.show();
						}

						@Override
						public void onError(ErrorResponse error){
							error.showToast(getActivity());
						}
					})
					.exec();
		}



		private ImageView imgForIndex(int index){
			if(index==0)
				return pic1;
			return pic2;
		}

		@Override
		public void setImage(int index, Bitmap bitmap) {
			if(index==0 && item.hosts.get(0).photoUrl==null)
				index=1;
			imgForIndex(index).setImageBitmap(bitmap);
		}

		@Override
		public void clearImage(int index){
			imgForIndex(index).setImageDrawable(placeholder);
		}


	}
}
