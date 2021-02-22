package me.grishka.houseclub.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.DateFormat;

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
import me.grishka.houseclub.api.methods.Follow;
import me.grishka.houseclub.api.methods.GetProfile;
import me.grishka.houseclub.api.methods.Unfollow;
import me.grishka.houseclub.api.methods.UpdateBio;
import me.grishka.houseclub.api.methods.UpdatePhoto;
import me.grishka.houseclub.api.methods.UpdateName;
import me.grishka.houseclub.api.model.FullUser;

public class ProfileFragment extends LoaderFragment{

	private static final int PICK_PHOTO_RESULT=468;

	private FullUser user;

	private TextView name, username, followers, following, followsYou, bio, inviteInfo, twitter, instagram;
	private ImageView photo, inviterPhoto;
	private Button followBtn;
	private View socialButtons;
	private boolean self;

	@Override
	public void onAttach(Activity activity){
		super.onAttach(activity);
		loadData();
		self=getArguments().getInt("id")==Integer.parseInt(ClubhouseSession.userID);
		if(self)
			setHasOptionsMenu(true);
	}

	@Override
	public View onCreateContentView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
		View v=inflater.inflate(R.layout.profile, container, false);

		name=v.findViewById(R.id.name);
		username=v.findViewById(R.id.username);
		followers=v.findViewById(R.id.followers);
		following=v.findViewById(R.id.following);
		followsYou=v.findViewById(R.id.follows_you);
		bio=v.findViewById(R.id.bio);
		inviteInfo=v.findViewById(R.id.invite_info);
		photo=v.findViewById(R.id.photo);
		inviterPhoto=v.findViewById(R.id.inviter_photo);
		followBtn=v.findViewById(R.id.follow_btn);
		twitter=v.findViewById(R.id.twitter);
		instagram=v.findViewById(R.id.instagram);
		socialButtons=v.findViewById(R.id.social);

		followBtn.setOnClickListener(this::onFollowClick);
		instagram.setOnClickListener(this::onInstagramClick);
		twitter.setOnClickListener(this::onTwitterClick);
		followers.setOnClickListener(this::onFollowersClick);
		following.setOnClickListener(this::onFollowingClick);
		v.findViewById(R.id.inviter_btn).setOnClickListener(this::onInviterClick);
		if(self){
			bio.setOnClickListener(this::onBioClick);
			photo.setOnClickListener(this::onPhotoClick);
			name.setOnClickListener(this::onNameClick);
		}

		return v;
	}

	@Override
	protected void doLoadData(){
		currentRequest=new GetProfile(getArguments().getInt("id"))
				.setCallback(new SimpleCallback<GetProfile.Response>(this){
					@Override
					public void onSuccess(GetProfile.Response result){
						currentRequest=null;
						user=result.userProfile;


						name.setText(user.name);
						username.setText('@'+user.username);
						ColorDrawable d=new ColorDrawable(0xFF808080);
						if(user.photoUrl!=null)
							ViewImageLoader.load(photo, d, user.photoUrl);
						else
							photo.setImageDrawable(d);

						followsYou.setVisibility(user.followsMe ? View.VISIBLE : View.GONE);
						followers.setText(getResources().getQuantityString(R.plurals.followers, user.numFollowers, user.numFollowers));
						following.setText(getResources().getQuantityString(R.plurals.following, user.numFollowing, user.numFollowing));
						bio.setText(user.bio);
						if(TextUtils.isEmpty(user.bio) && self)
							bio.setText(R.string.update_bio);

						if(self)
							followBtn.setVisibility(View.GONE);
						else
							followBtn.setText(user.isFollowed() ? R.string.following : R.string.follow);

						if(user.twitter==null && user.instagram==null){
							socialButtons.setVisibility(View.GONE);
						}else{
							socialButtons.setVisibility(View.VISIBLE);
							twitter.setVisibility(user.twitter==null ? View.GONE : View.VISIBLE);
							instagram.setVisibility(user.instagram==null ? View.GONE : View.VISIBLE);
							if(user.twitter!=null)
								twitter.setText(user.twitter);
							if(user.instagram!=null)
								instagram.setText(user.instagram);
						}

						String joined=getString(R.string.joined_date, DateFormat.getDateInstance().format(user.timeCreated));
						if(user.invitedByUserProfile!=null){
							ColorDrawable d2=new ColorDrawable(0xFF808080);
							joined+="\n"+getString(R.string.invited_by, user.invitedByUserProfile.name);
							if(user.invitedByUserProfile.photoUrl!=null)
								ViewImageLoader.load(inviterPhoto, d2, user.invitedByUserProfile.photoUrl);
							else
								inviterPhoto.setImageDrawable(d2);
						}else{
							inviterPhoto.setVisibility(View.GONE);
						}
						inviteInfo.setText(joined);

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

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data){
		if(requestCode==PICK_PHOTO_RESULT && resultCode==Activity.RESULT_OK){
			new UpdatePhoto(data.getData())
					.wrapProgress(getActivity())
					.setCallback(new Callback<Bitmap>(){
						@Override
						public void onSuccess(Bitmap result){
							photo.setImageBitmap(result);
						}

						@Override
						public void onError(ErrorResponse error){
							error.showToast(getActivity());
						}
					})
					.exec();
		}
	}

	private void onFollowClick(View v){
		if(user.isFollowed()){
			new AlertDialog.Builder(getActivity())
					.setMessage(getString(R.string.confirm_unfollow, user.name))
					.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener(){
						@Override
						public void onClick(DialogInterface dialogInterface, int i){
							new Unfollow(user.userId)
									.wrapProgress(getActivity())
									.setCallback(new Callback<BaseResponse>(){
										@Override
										public void onSuccess(BaseResponse result){
											user.notificationType=0;
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
			new Follow(user.userId)
					.wrapProgress(getActivity())
					.setCallback(new Callback<BaseResponse>(){
						@Override
						public void onSuccess(BaseResponse result){
							user.notificationType=2;
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

	private void onInstagramClick(View v){
		startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://instagram.com/"+user.instagram)));
	}

	private void onTwitterClick(View v){
		startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://twitter.com/"+user.twitter)));
	}

	private void onFollowersClick(View v){
		Bundle args=new Bundle();
		args.putInt("id", user.userId);
		Nav.go(getActivity(), FollowersFragment.class, args);
	}

	private void onFollowingClick(View v){
		Bundle args=new Bundle();
		args.putInt("id", user.userId);
		Nav.go(getActivity(), FollowingFragment.class, args);
	}

	private void onInviterClick(View v){
		if(user.invitedByUserProfile==null)
			return;
		Bundle args=new Bundle();
		args.putInt("id", user.invitedByUserProfile.userId);
		Nav.go(getActivity(), ProfileFragment.class, args);
	}

	private void onNameClick(View v) {
		final EditText edit = new EditText(getActivity());
		edit.setInputType(InputType.TYPE_TEXT_FLAG_CAP_WORDS | InputType.TYPE_TEXT_VARIATION_PERSON_NAME);
		edit.setText(user.name);
		new AlertDialog.Builder(getActivity())
				.setTitle(R.string.update_name)
				.setView(edit)
				.setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						final String newName = edit.getText().toString();
						new UpdateName(newName)
								.wrapProgress(getActivity())
								.setCallback(new Callback<BaseResponse>() {
									@Override
									public void onSuccess(BaseResponse result) {
										user.name = newName;

										if (TextUtils.isEmpty((newName)))
											name.setText(R.string.update_name);
										else
											name.setText(newName);
									}

									@Override
									public void onError(ErrorResponse error) {
										error.showToast(getActivity());
									}
								})
								.exec();
					}
				})
				.setNegativeButton(R.string.cancel, null)
				.show();
	}

	private void onBioClick(View v){
		final EditText edit=new EditText(getActivity());
		edit.setInputType(InputType.TYPE_TEXT_FLAG_CAP_SENTENCES | InputType.TYPE_TEXT_FLAG_MULTI_LINE | edit.getInputType());
		edit.setSingleLine(false);
		edit.setMinLines(3);
		edit.setMaxLines(6);
		edit.setGravity(Gravity.TOP);
		edit.setText(user.bio);
		new AlertDialog.Builder(getActivity())
				.setTitle(R.string.update_bio)
				.setView(edit)
				.setPositiveButton(R.string.save, new DialogInterface.OnClickListener(){
					@Override
					public void onClick(DialogInterface dialogInterface, int i){
						final String newBio=edit.getText().toString();
						new UpdateBio(newBio)
								.wrapProgress(getActivity())
								.setCallback(new Callback<BaseResponse>(){
									@Override
									public void onSuccess(BaseResponse result){
										user.bio=newBio;
										if(TextUtils.isEmpty(newBio))
											bio.setText(R.string.update_bio);
										else
											bio.setText(newBio);
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

	private void onPhotoClick(View v){
		Intent intent=new Intent(Intent.ACTION_GET_CONTENT);
		intent.setType("image/*");
		startActivityForResult(intent, PICK_PHOTO_RESULT);
	}
}
