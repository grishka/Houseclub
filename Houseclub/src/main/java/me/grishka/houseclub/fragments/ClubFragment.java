package me.grishka.houseclub.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.stream.Collectors;

import me.grishka.appkit.Nav;
import me.grishka.appkit.api.Callback;
import me.grishka.appkit.api.ErrorResponse;
import me.grishka.appkit.api.SimpleCallback;
import me.grishka.appkit.fragments.LoaderFragment;
import me.grishka.appkit.imageloader.ViewImageLoader;
import me.grishka.houseclub.R;
import me.grishka.houseclub.VoiceService;
import me.grishka.houseclub.api.BaseResponse;
import me.grishka.houseclub.api.ClubhouseSession;
import me.grishka.houseclub.api.methods.FollowClub;
import me.grishka.houseclub.api.methods.GetClub;
import me.grishka.houseclub.api.methods.UnfollowClub;
import me.grishka.houseclub.api.model.Club;

public class ClubFragment extends LoaderFragment{

	private static final int PICK_PHOTO_RESULT=468;

	private Club club;

	private boolean is_follower;
	private TextView name, description, followers, members, topics , user_clubs;
	private ImageView clubPhoto ;
	private Button followBtn;

	@Override
	public void onAttach(Activity activity){
		super.onAttach(activity);
		loadData();
	}

	@Override
	public View onCreateContentView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
		View v=inflater.inflate(R.layout.club, container, false);

		name=v.findViewById(R.id.name);
		description=v.findViewById(R.id.description);
		followers=v.findViewById(R.id.followers);
		members=v.findViewById(R.id.members);
		topics=v.findViewById(R.id.topics);
		clubPhoto=v.findViewById(R.id.clubPhoto);

		followBtn=v.findViewById(R.id.follow_btn);

		followBtn.setOnClickListener(this::onFollowClick);

		return v;
	}

	@Override
	protected void doLoadData(){

		currentRequest=new GetClub(getArguments().getInt("id"))
				.setCallback(new SimpleCallback<GetClub.Response>(this){
					@Override
					public void onSuccess(GetClub.Response result){
						currentRequest=null;

						club = result.club;

						is_follower = result.is_follower;

						name.setText(club.name);
						description.setText(result.club.description);
						followers.setText(( club.num_followers>0 ?  String.valueOf(club.num_followers) : "0") + " followers");
						members.setText(( club.num_members>0 ?  String.valueOf(club.num_members) : "0") + " members");

						followBtn.setText(is_follower ? R.string.following : R.string.follow);

						topics.setText(result.topics.stream().map(topic->topic.title ).collect(Collectors.joining(" . ")) );

						if(club.photo_url!=null){
							ColorDrawable d2=new ColorDrawable(getResources().getColor(R.color.grey));
							if(club.photo_url!=null)
								ViewImageLoader.load(clubPhoto, d2, club.photo_url);
							else
								clubPhoto.setImageDrawable(d2);
						}else{
							clubPhoto.setVisibility(View.GONE);
						}


						dataLoaded();

					}
				})
				.exec();



	}

	@Override
	public void onRefresh(){
		loadData();
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

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater){
		menu.add(R.string.log_out);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item){
		if(VoiceService.getInstance()!=null){
			VoiceService.getInstance().leaveChannel();
		}
		ClubhouseSession.userID=ClubhouseSession.userToken=null;
		ClubhouseSession.write();
		Nav.goClearingStack(getActivity(), LoginFragment.class, null);
		return true;
	}

	private void onFollowClick(View v){
		if(is_follower){
			new AlertDialog.Builder(getActivity())
					.setMessage(getString(R.string.confirm_unfollow, club.name))
					.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener(){
						@Override
						public void onClick(DialogInterface dialogInterface, int i){
							new UnfollowClub(club.club_id)
									.wrapProgress(getActivity())
									.setCallback(new Callback<BaseResponse>(){
										@Override
										public void onSuccess(BaseResponse result){
											followBtn.setText(R.string.follow);
										}

										@Override
										public void onError(ErrorResponse error){
											error.showToast(getActivity());
										}
									})
									.exec();
						}
					})
					.setNegativeButton(R.string.no, null)
					.show();
		}else{
			new FollowClub(club.club_id)
					.wrapProgress(getActivity())
					.setCallback(new Callback<BaseResponse>(){
						@Override
						public void onSuccess(BaseResponse result){
							followBtn.setText(R.string.following);
						}

						@Override
						public void onError(ErrorResponse error){
							error.showToast(getActivity());
						}
					})
					.exec();
		}
	}

}
