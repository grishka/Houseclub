package me.grishka.houseclub.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.util.HashMap;
import java.util.stream.Collectors;

import me.grishka.appkit.Nav;
import me.grishka.appkit.api.Callback;
import me.grishka.appkit.api.ErrorResponse;
import me.grishka.appkit.api.SimpleCallback;
import me.grishka.appkit.fragments.LoaderFragment;
import me.grishka.appkit.imageloader.ViewImageLoader;
import me.grishka.houseclub.BuildConfig;
import me.grishka.houseclub.R;
import me.grishka.houseclub.VoiceService;
import me.grishka.houseclub.api.BaseResponse;
import me.grishka.houseclub.api.ClubhouseAPIController;
import me.grishka.houseclub.api.ClubhouseSession;
import me.grishka.houseclub.api.methods.Follow;
import me.grishka.houseclub.api.methods.GetClub;
import me.grishka.houseclub.api.methods.GetProfile;
import me.grishka.houseclub.api.methods.InviteToApp;
import me.grishka.houseclub.api.methods.Me;
import me.grishka.houseclub.api.methods.Unfollow;
import me.grishka.houseclub.api.methods.UpdateBio;
import me.grishka.houseclub.api.methods.UpdateInstagram;
import me.grishka.houseclub.api.methods.UpdatePhoto;
import me.grishka.houseclub.api.methods.UpdateName;
import me.grishka.houseclub.api.model.FullUser;

public class ProfileFragment extends LoaderFragment{

	private static final int PICK_PHOTO_RESULT=468;

	private FullUser user;

	private TextView name, username, followers, following, followsYou, bio, inviteInfo, twitter, instagram,
			invites , mutuals , followed_by , user_clubs , member_of_text;
	private ImageView photo, inviterPhoto , pic1 , pic2 , pic3 ;
	private Button followBtn, inviteButton;
	private EditText invitePhoneNum;
	private View socialButtons, inviteLayout;
	private WebView webView;

	private boolean self , isImageFitToScreen;

	@Override
	public void onAttach(Activity activity){
		super.onAttach(activity);
		loadData();
		self=getArguments().getInt("id")==Integer.parseInt(ClubhouseSession.userID);
		isImageFitToScreen=true;
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
		pic1=v.findViewById(R.id.pic1);
		pic2=v.findViewById(R.id.pic2);
		pic3=v.findViewById(R.id.pic3);

		followBtn=v.findViewById(R.id.follow_btn);
		twitter=v.findViewById(R.id.twitter);
		instagram=v.findViewById(R.id.instagram);
		webView = v.findViewById(R.id.webView);

		socialButtons=v.findViewById(R.id.social);
		inviteLayout = v.findViewById(R.id.invite_layout);
		inviteButton = v.findViewById(R.id.invite_button);
		invites = v.findViewById(R.id.num_of_invites);
		mutuals = v.findViewById(R.id.mutuals);
		user_clubs = v.findViewById(R.id.user_clubs);
		member_of_text = v.findViewById(R.id.member_of_text);
		followed_by = v.findViewById(R.id.followed_by);
		invitePhoneNum = v.findViewById(R.id.invite_phone_num);

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
			inviteButton.setOnClickListener(this::onInviteClick);
		}else {
			photo.setOnClickListener(this::onForeignPhotoClick);
		}


		webView.setVisibility(View.GONE);
		webView.getSettings().setJavaScriptEnabled(true);
		webView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);

		return v;
	}

	private void onForeignPhotoClick(View view) {
		if(isImageFitToScreen) {
			followBtn.setVisibility(View.GONE);
			isImageFitToScreen=false;
			photo.setLayoutParams(new LinearLayout.LayoutParams((int) (272 * (getResources().getDisplayMetrics().density)), (int) (272 * (getResources().getDisplayMetrics().density))));
			photo.setAdjustViewBounds(true);
		}else{
			followBtn.setVisibility(View.VISIBLE);
			isImageFitToScreen=true;
			photo.setLayoutParams(new LinearLayout.LayoutParams((int) (72 * (getResources().getDisplayMetrics().density)), (int) (72 * (getResources().getDisplayMetrics().density))));
			photo.setScaleType(ImageView.ScaleType.FIT_XY);
		}
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
						ColorDrawable d=new ColorDrawable(getResources().getColor(R.color.grey));
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

						if(!self && user.twitter==null && user.instagram==null){
							socialButtons.setVisibility(View.GONE);
						}else{
							socialButtons.setVisibility(View.VISIBLE);
							twitter.setVisibility(user.twitter==null ? View.GONE : View.VISIBLE);
							instagram.setVisibility(user.instagram==null && !self ? View.GONE : View.VISIBLE);
							if(user.twitter!=null)
								twitter.setText(user.twitter);
							if(user.instagram!=null)
								instagram.setText(user.instagram);
							if(self && user.instagram==null)
								instagram.setText(R.string.add_instagram);
						}

						String joined=getString(R.string.joined_date, DateFormat.getDateInstance().format(user.timeCreated));
						if(user.invitedByUserProfile!=null){
							ColorDrawable d2=new ColorDrawable(getResources().getColor(R.color.grey));
							joined+="\n"+getString(R.string.invited_by, user.invitedByUserProfile.name);
							if(user.invitedByUserProfile.photoUrl!=null)
								ViewImageLoader.load(inviterPhoto, d2, user.invitedByUserProfile.photoUrl);
							else
								inviterPhoto.setImageDrawable(d2);
						}else{
							inviterPhoto.setVisibility(View.GONE);
						}

						if( user.mutualFollows.size() > 0 ){

							followed_by.setText(" Followed by ");

							String usernames = "";

							for(int i = 0 ; i< user.mutualFollows.size(); i++){

								if(i>0){
									usernames += ", "+user.mutualFollows.get(i).username;
								}else{
									usernames += user.mutualFollows.get(i).username;
								}

								if(i == 0){
									if(user.mutualFollows.get(i).photoUrl != null){
										ColorDrawable d2=new ColorDrawable(getResources().getColor(R.color.grey));
										if(user.mutualFollows.get(i).photoUrl!=null) {
											pic1.setVisibility(View.VISIBLE);
											ViewImageLoader.load(pic1, d2, user.mutualFollows.get(i).photoUrl);
										}else{
											pic1.setImageDrawable(d2);
										}
									}else{
										pic1.setVisibility(View.GONE);
									}
								}

								if(i == 1){
									if(user.mutualFollows.get(i).photoUrl !=null){
										ColorDrawable d2=new ColorDrawable(getResources().getColor(R.color.grey));
										if(user.mutualFollows.get(i).photoUrl!=null) {
											pic2.setVisibility(View.VISIBLE);
											ViewImageLoader.load(pic2, d2, user.mutualFollows.get(i).photoUrl);
										}else{
											pic2.setImageDrawable(d2);
										}
									}else{
										pic2.setVisibility(View.GONE);
									}
								}

								if(i == 2){

									if( user.mutualFollows.get(i).photoUrl !=null){
										ColorDrawable d2=new ColorDrawable(getResources().getColor(R.color.grey));
										if(user.mutualFollows.get(i).photoUrl!=null) {
											pic3.setVisibility(View.VISIBLE);
											ViewImageLoader.load(pic3, d2, user.mutualFollows.get(i).photoUrl);
										}else{
											pic3.setImageDrawable(d2);
										}
									}else{
										pic3.setVisibility(View.GONE);
									}

								}

							}

							if((user.mutual_follows_count - 3) > 0){
								usernames += " and " + (user.mutual_follows_count - 3) +  " others";
							}

							mutuals.setText(usernames);

						}

						inviteInfo.setText(joined);

						if(user.clubs.size() > 0){

							member_of_text.setText("Member of");
							user_clubs.setText(result.userProfile.clubs.stream().map(club->club.name ).
									collect(Collectors.joining(" . ")) );



						}



						dataLoaded();

					}
				})
				.exec();

		loadInvites();
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

	private void loadInvites() {
		new Me().setCallback(new Callback<Me.Response>() {
			@Override
			public void onSuccess(Me.Response result) {
				if (self && result.num_invites > 0) {
					invites.setText(getResources().getQuantityString(R.plurals.invites, result.num_invites, result.num_invites));
					inviteLayout.setVisibility(View.VISIBLE);
				} else {
					inviteLayout.setVisibility(View.GONE);
				}
			}

			@Override
			public void onError(ErrorResponse error) {
				inviteLayout.setVisibility(View.GONE);
			}
		}).exec();
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
		if (self){
			HashMap<String, String> headers = new HashMap<>();
			headers.put("CH-AppBuild", ClubhouseAPIController.API_BUILD_ID);
			headers.put("CH-AppVersion", ClubhouseAPIController.API_BUILD_VERSION);
			headers.put("User-Agent", ClubhouseAPIController.API_UA);

			headers.put("CH-DeviceId", ClubhouseSession.deviceID);
			headers.put("Authorization", "Token "+ClubhouseSession.userToken);
			headers.put("CH-UserID", ClubhouseSession.userID);

			if (user.instagram == null) {
				webView.setVisibility(View.VISIBLE);
				webView.setWebViewClient(new WebViewClient() {
					@Override
					public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
						Boolean redirect = checkRedirect(request.getUrl().toString());
						view.loadUrl(request.getUrl().toString());
						return redirect;
					}

					public boolean shouldOverrideUrlLoading(WebView view, String url) {
						Boolean redirect = checkRedirect(url);
						view.loadUrl(url);
						return redirect;
					}
				});
				webView.loadUrl(
						"https://www.instagram.com/oauth/authorize?client_id=" +
								"1352866981588597" +
								"&redirect_uri=" + UpdateInstagram.REDIRECT_INSTAGRAM_URL +
								"&scope=user_profile" +
								"&response_type=code",
						headers
				);
			} else {
				new AlertDialog.Builder(getActivity())
						.setMessage(getString(R.string.confirm_unlink_instagram_title))
						.setMessage(getString(R.string.confirm_unlink_instagram))
						.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener(){
							@Override
							public void onClick(DialogInterface dialogInterface, int i){
								new UpdateInstagram(null)
										.wrapProgress(getActivity())
										.setCallback(new Callback<BaseResponse>(){
											@Override
											public void onSuccess(BaseResponse result){
												instagram.setText(R.string.add_instagram);
												webView.setVisibility(View.GONE);
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
			}
		} else
		startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://instagram.com/"+user.instagram)));
	}

	private Boolean checkRedirect(String url){
		if (url.startsWith(UpdateInstagram.REDIRECT_INSTAGRAM_URL)) {

			// last2 chars is #_ by docs https://developers.facebook.com/docs/instagram-basic-display-api/getting-started
			String code = url.substring((UpdateInstagram.REDIRECT_INSTAGRAM_URL+ "?code=").length(), url.length()-2);

			new UpdateInstagram(code)
					.wrapProgress(getActivity())
					.setCallback(new Callback<BaseResponse>(){
						@Override
						public void onSuccess(BaseResponse result){
							instagram.setText(R.string.instagram_linked);
							webView.setVisibility(View.GONE);
						}

						@Override
						public void onError(ErrorResponse error){
							error.showToast(getActivity());
						}
					})
					.exec();
			return false;
		} else {
			return true;
		}
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

	private void onInviteClick(View v) {
		final String numberToInvite = invitePhoneNum.getText().toString();
		new InviteToApp("", numberToInvite, "")
				.wrapProgress(getContext())
				.setCallback(new Callback<BaseResponse>() {
					@Override
					public void onSuccess(BaseResponse result) {
						Toast.makeText(getContext(), "success", Toast.LENGTH_SHORT).show();
						loadInvites();
					}

					@Override
					public void onError(ErrorResponse error) {
						Toast.makeText(getContext(), "failed", Toast.LENGTH_SHORT).show();
						loadInvites();
					}
				})
				.exec();
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
