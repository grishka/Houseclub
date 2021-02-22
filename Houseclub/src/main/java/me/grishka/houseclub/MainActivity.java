package me.grishka.houseclub;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;

import java.util.Date;
import java.util.List;

import androidx.annotation.NonNull;
import me.grishka.appkit.FragmentStackActivity;
import me.grishka.appkit.api.Callback;
import me.grishka.appkit.api.ErrorResponse;
import me.grishka.appkit.api.SimpleCallback;
import me.grishka.houseclub.api.BaseResponse;
import me.grishka.houseclub.api.ClubhouseSession;
import me.grishka.houseclub.api.methods.CheckWaitlistStatus;
import me.grishka.houseclub.api.methods.GetChannel;
import me.grishka.houseclub.api.methods.GetEvent;
import me.grishka.houseclub.api.methods.JoinChannel;
import me.grishka.houseclub.api.model.Channel;
import me.grishka.houseclub.fragments.HomeFragment;
import me.grishka.houseclub.fragments.InChannelFragment;
import me.grishka.houseclub.fragments.LoginFragment;
import me.grishka.houseclub.fragments.RegisterFragment;
import me.grishka.houseclub.fragments.WaitlistedFragment;

public class MainActivity extends FragmentStackActivity{

	private Channel channelToJoin;
	private static final int PERMISSION_RESULT=270;

	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);

		SharedPreferences prefs=getPreferences(MODE_PRIVATE);
		if(!prefs.getBoolean("warningShown", false)){
			new AlertDialog.Builder(this)
					.setTitle(R.string.warning)
					.setMessage(R.string.warning_text)
					.setPositiveButton(R.string.i_understand, null)
					.setCancelable(false)
					.show();
			prefs.edit().putBoolean("warningShown", true).apply();
		}

		if(ClubhouseSession.isLoggedIn()){
			showFragment(ClubhouseSession.isWaitlisted ? new WaitlistedFragment() : new HomeFragment());
			if(ClubhouseSession.isWaitlisted){
				new CheckWaitlistStatus()
						.setCallback(new Callback<CheckWaitlistStatus.Response>(){
							@Override
							public void onSuccess(CheckWaitlistStatus.Response result){
								if(!result.isWaitlisted){
									ClubhouseSession.isWaitlisted=false;
									ClubhouseSession.write();
									if(result.isOnboarding){
//										showFragmentClearingBackStack(new RegisterFragment());
										new AlertDialog.Builder(MainActivity.this)
												.setMessage(R.string.log_in_to_activate)
												.setPositiveButton(R.string.ok, null)
												.show();
										ClubhouseSession.userID=ClubhouseSession.userToken=null;
										ClubhouseSession.write();
										showFragmentClearingBackStack(new LoginFragment());
									}else{
										showFragmentClearingBackStack(new HomeFragment());
									}
//									if(Intent.ACTION_VIEW.equals(getIntent().getAction())){
//										joinChannelFromIntent();
//									}
								}
							}

							@Override
							public void onError(ErrorResponse error){

							}
						})
						.exec();
			}else{
				if(Intent.ACTION_VIEW.equals(getIntent().getAction())){
					joinChannelFromIntent();
				}
			}
		}else{
			showFragment(new LoginFragment());
		}
	}

	@Override
	protected void onNewIntent(Intent intent){
		super.onNewIntent(intent);
		setIntent(intent);
		if(Intent.ACTION_VIEW.equals(intent.getAction())){
			joinChannelFromIntent();
		}else if(intent.hasExtra("openCurrentChannel")){
			if(VoiceService.getInstance()!=null){
				Bundle extras=new Bundle();
				extras.putBoolean("_can_go_back", true);
				InChannelFragment fragment=new InChannelFragment();
				fragment.setArguments(extras);
				showFragment(fragment);
			}
		}
	}

	private void joinChannelFromIntent(){
		Uri data=getIntent().getData();
		List<String> path=data.getPathSegments();
		String id=path.get(path.size()-1);
		if(path.get(0).equals("room")){
			joinChannelById(id);
		}else if(path.get(0).equals("event")){
			new GetEvent(id)
					.wrapProgress(this)
					.setCallback(new Callback<GetEvent.Response>(){
						@Override
						public void onSuccess(GetEvent.Response result){
							if(result.event.channel!=null){
								joinChannelById(result.event.channel);
							}else{
								if(result.event.isExpired)
									Toast.makeText(MainActivity.this, R.string.event_expired, Toast.LENGTH_SHORT).show();
								else if(result.event.timeStart.after(new Date()))
									Toast.makeText(MainActivity.this, R.string.event_not_started, Toast.LENGTH_SHORT).show();
							}
						}

						@Override
						public void onError(ErrorResponse error){
							error.showToast(MainActivity.this);
						}
					})
					.exec();
		}
	}

	private void joinChannelById(String id){
		new GetChannel(id)
				.wrapProgress(this)
				.setCallback(new Callback<Channel>(){
					@Override
					public void onSuccess(final Channel result){
						new AlertDialog.Builder(MainActivity.this)
								.setTitle(R.string.join_this_room)
								.setMessage(result.topic)
								.setPositiveButton(R.string.join, new DialogInterface.OnClickListener(){
									@Override
									public void onClick(DialogInterface dialogInterface, int i){
										joinChannel(result);
									}
								})
								.setNegativeButton(R.string.cancel, null)
								.show();
					}

					@Override
					public void onError(ErrorResponse error){
						error.showToast(MainActivity.this);
					}
				})
				.exec();
	}

	public void joinChannel(Channel chan){
		if(VoiceService.getInstance()!=null){
			Channel current=VoiceService.getInstance().getChannel();
			if(current.channel.equals(chan.channel)){
				Bundle extras=new Bundle();
				extras.putBoolean("_can_go_back", true);
				InChannelFragment fragment=new InChannelFragment();
				fragment.setArguments(extras);
				showFragment(fragment);
				return;
			}
			VoiceService.getInstance().leaveChannel();
		}
		if(checkSelfPermission(Manifest.permission.RECORD_AUDIO)==PackageManager.PERMISSION_GRANTED){
			new JoinChannel(chan.channel)
					.wrapProgress(this)
					.setCallback(new Callback<Channel>(){
						@Override
						public void onSuccess(Channel result){
							Intent intent=new Intent(MainActivity.this, VoiceService.class);
							intent.putExtra("channel", result.channel);
							DataProvider.saveChannel(result);
							if(Build.VERSION.SDK_INT>=26)
								startForegroundService(intent);
							else
								startService(intent);

							Bundle extras=new Bundle();
							extras.putBoolean("_can_go_back", true);
							InChannelFragment fragment=new InChannelFragment();
							fragment.setArguments(extras);
							showFragment(fragment);
						}

						@Override
						public void onError(ErrorResponse error){
							error.showToast(MainActivity.this);
						}
					})
					.exec();
		}else{
			channelToJoin=chan;
			requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO}, PERMISSION_RESULT);
		}
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults){
		if(requestCode==PERMISSION_RESULT && grantResults[0]==PackageManager.PERMISSION_GRANTED){
			if(channelToJoin!=null){
				joinChannel(channelToJoin);
			}
		}
		channelToJoin=null;
	}
}
