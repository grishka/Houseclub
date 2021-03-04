package me.grishka.houseclub;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.drawable.Icon;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import com.google.gson.JsonObject;
import com.pubnub.api.PNConfiguration;
import com.pubnub.api.PubNub;
import com.pubnub.api.callbacks.SubscribeCallback;
import com.pubnub.api.models.consumer.PNStatus;
import com.pubnub.api.models.consumer.objects_api.channel.PNChannelMetadataResult;
import com.pubnub.api.models.consumer.objects_api.membership.PNMembershipResult;
import com.pubnub.api.models.consumer.objects_api.uuid.PNUUIDMetadataResult;
import com.pubnub.api.models.consumer.pubsub.PNMessageResult;
import com.pubnub.api.models.consumer.pubsub.PNPresenceEventResult;
import com.pubnub.api.models.consumer.pubsub.PNSignalResult;
import com.pubnub.api.models.consumer.pubsub.files.PNFileEventResult;
import com.pubnub.api.models.consumer.pubsub.message_actions.PNMessageActionResult;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import androidx.annotation.Nullable;
import io.agora.rtc.Constants;
import io.agora.rtc.IRtcEngineEventHandler;
import io.agora.rtc.RtcEngine;
import me.grishka.appkit.api.Callback;
import me.grishka.appkit.api.ErrorResponse;
import me.grishka.houseclub.api.BaseResponse;
import me.grishka.houseclub.api.ClubhouseAPIController;
import me.grishka.houseclub.api.ClubhouseSession;
import me.grishka.houseclub.api.methods.ActivePing;
import me.grishka.houseclub.api.methods.AudienceReply;
import me.grishka.houseclub.api.methods.JoinChannel;
import me.grishka.houseclub.api.methods.LeaveChannel;
import me.grishka.houseclub.api.model.Channel;
import me.grishka.houseclub.api.model.ChannelUser;
import me.grishka.houseclub.notification.NotificationHandlerBroadcastReceiver;

public class VoiceService extends Service{

	private RtcEngine engine;
	private Channel channel;
	private boolean muted=true;
	private Handler uiHandler=new Handler(Looper.getMainLooper());
	private Runnable pinger=new Runnable(){
		@Override
		public void run(){
			new ActivePing(channel.channel).exec();
			uiHandler.postDelayed(this, 30000);
		}
	};
	private boolean raisedHand=false;
	private PubNub pubnub;
	private ArrayList<Integer> mutedUserIds=new ArrayList<>();
	private boolean isSelfSpeaker, isSelfModerator;

	private static ArrayList<ChannelEventListener> listeners=new ArrayList<>();

	private static VoiceService instance;
	private static final String TAG="VoiceService";

	public static VoiceService getInstance(){
		return instance;
	}

	@Nullable
	@Override
	public IBinder onBind(Intent intent){
		return null;
	}

	@Override
	public void onCreate(){
		super.onCreate();

		try{
			engine=RtcEngine.create(getBaseContext(), ClubhouseAPIController.AGORA_KEY, new RtcEngineEventHandler());
		}catch(Exception x){
			Log.e(TAG, "Error initializing agora", x);
			stopSelf();
			return;
		}

		engine.setDefaultAudioRoutetoSpeakerphone(true);
		engine.enableAudioVolumeIndication(500, 3, false);
		engine.muteLocalAudioStream(true);
		instance=this;
	}

	@Override
	public void onDestroy(){
		super.onDestroy();
		RtcEngine.destroy();
		instance=null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId){
		if(engine!=null){
			String id = intent.getStringExtra("channel");
			channel=DataProvider.getChannel(id);
			updateChannel(channel);

			Intent snoozeIntent = new Intent(this, NotificationHandlerBroadcastReceiver.class);
			snoozeIntent.setAction(NotificationHandlerBroadcastReceiver.ACTION_LEAVE_ROOM);
			PendingIntent leaveRoomPendingIntent = PendingIntent.getBroadcast(this, 0, snoozeIntent, 0);
			Notification.Action leaveRoomAction = new Notification.Action.Builder(
					Icon.createWithResource(this, R.drawable.ic_leave),
					getString(R.string.leave_room),
					leaveRoomPendingIntent
			).build();

			NotificationManager nm=getSystemService(NotificationManager.class);
			Notification.Builder n=new Notification.Builder(this)
					.setSmallIcon(R.drawable.ic_phone_in_talk)
					.setContentTitle(getString(R.string.ongoing_call))
					.setContentText(intent.getStringExtra("topic"))
					.setContentIntent(PendingIntent.getActivity(this, 1, new Intent(this, MainActivity.class).putExtra("openCurrentChannel", true), PendingIntent.FLAG_UPDATE_CURRENT))
					.addAction(leaveRoomAction);
			if(Build.VERSION.SDK_INT>=26){
				if(nm.getNotificationChannel("ongoing")==null){
					NotificationChannel nc=new NotificationChannel("ongoing", "Ongoing calls", NotificationManager.IMPORTANCE_LOW);
					nm.createNotificationChannel(nc);
				}
				n.setChannelId("ongoing");
			}
			startForeground(10, n.build());

			doJoinChannel();
		}
		return START_NOT_STICKY;
	}

	private void doJoinChannel(){
		engine.setChannelProfile(isSelfSpeaker ? Constants.CHANNEL_PROFILE_COMMUNICATION : Constants.CHANNEL_PROFILE_LIVE_BROADCASTING);
		engine.joinChannel(channel.token, channel.channel, "", Integer.parseInt(ClubhouseSession.userID));
		uiHandler.postDelayed(pinger, 30000);
		for(ChannelEventListener l:listeners)
			l.onChannelUpdated(channel);

		PNConfiguration pnConf=new PNConfiguration();
		pnConf.setSubscribeKey(ClubhouseAPIController.PUBNUB_SUB_KEY);
		pnConf.setPublishKey(ClubhouseAPIController.PUBNUB_PUB_KEY);
		//pnConf.setUuid(UUID.randomUUID().toString());
		pnConf.setOrigin("clubhouse.pubnub.com");
		pnConf.setUuid(ClubhouseSession.userID);
		pnConf.setPresenceTimeoutWithCustomInterval(channel.pubnubHeartbeatValue, channel.pubnubHeartbeatInterval);
		pnConf.setAuthKey(channel.pubnubToken);

		pubnub=new PubNub(pnConf);
		pubnub.addListener(new SubscribeCallback(){
			@Override
			public void status(PubNub pubnub, PNStatus pnStatus){
				Log.d(TAG, "status() called with: pubnub = ["+pubnub+"], pnStatus = ["+pnStatus+"]");
			}

			@Override
			public void message(PubNub pubnub, PNMessageResult pnMessageResult){
				Log.d(TAG, "message() called with: pubnub = ["+pubnub+"], pnMessageResult = ["+pnMessageResult+"]");
				JsonObject msg=pnMessageResult.getMessage().getAsJsonObject();
				String act=msg.get("action").getAsString();
				switch(act){
					case "invite_speaker":
						onInvitedAsSpeaker(msg);
						break;
					case "join_channel":
						onUserJoined(msg);
						break;
					case "leave_channel":
						onUserLeft(msg);
						break;
					case "end_channel":
						onEndChannel(msg);
						break;
				}
			}

			@Override
			public void presence(PubNub pubnub, PNPresenceEventResult pnPresenceEventResult){
				Log.d(TAG, "presence() called with: pubnub = ["+pubnub+"], pnPresenceEventResult = ["+pnPresenceEventResult+"]");
			}

			@Override
			public void signal(PubNub pubnub, PNSignalResult pnSignalResult){
				Log.d(TAG, "signal() called with: pubnub = ["+pubnub+"], pnSignalResult = ["+pnSignalResult+"]");
			}

			@Override
			public void uuid(PubNub pubnub, PNUUIDMetadataResult pnUUIDMetadataResult){

			}

			@Override
			public void channel(PubNub pubnub, PNChannelMetadataResult pnChannelMetadataResult){
				Log.d(TAG, "channel() called with: pubnub = ["+pubnub+"], pnChannelMetadataResult = ["+pnChannelMetadataResult+"]");
			}

			@Override
			public void membership(PubNub pubnub, PNMembershipResult pnMembershipResult){
				Log.d(TAG, "membership() called with: pubnub = ["+pubnub+"], pnMembershipResult = ["+pnMembershipResult+"]");
			}

			@Override
			public void messageAction(PubNub pubnub, PNMessageActionResult pnMessageActionResult){
				Log.d(TAG, "messageAction() called with: pubnub = ["+pubnub+"], pnMessageActionResult = ["+pnMessageActionResult+"]");
			}

			@Override
			public void file(PubNub pubnub, PNFileEventResult pnFileEventResult){

			}
		});
		pubnub.subscribe().channels(Arrays.asList(
				"users."+ClubhouseSession.userID,
				"channel_user."+channel.channel+"."+ClubhouseSession.userID,
//				"channel_speakers."+channel.channel,
				"channel_all."+channel.channel
		)).execute();
	}

	public void leaveChannel(){
		engine.leaveChannel();
		new LeaveChannel(channel.channel)
				.exec();
		stopSelf();
		uiHandler.removeCallbacks(pinger);
		pubnub.unsubscribeAll();
		pubnub.destroy();
		uiHandler.post(() -> {
			for(ChannelEventListener l:listeners)
				l.onSelfLeft();
		});
	}

	public void leaveCurrentChannel(){
		uiHandler.post(() -> {
			for(ChannelEventListener l:listeners)
				l.onChannelEnded();
		});
		leaveChannel();
	}

	public void rejoinChannel(){
		engine.leaveChannel();
		pubnub.unsubscribeAll();
		new LeaveChannel(channel.channel)
				.setCallback(new Callback<BaseResponse>(){
					@Override
					public void onSuccess(BaseResponse result){
						new JoinChannel(channel.channel)
								.setCallback(new Callback<Channel>(){
									@Override
									public void onSuccess(Channel result){
										updateChannel(result);
										doJoinChannel();
									}

									@Override
									public void onError(ErrorResponse error){

									}
								})
								.exec();
					}

					@Override
					public void onError(ErrorResponse error){

					}
				})
				.exec();
	}

	public void raiseHand(){
		if(!raisedHand){
			raisedHand=true;
			new AudienceReply(channel.channel, true)
					.exec();
		}
	}

	public void unraiseHand(){
		if(raisedHand){
			raisedHand=false;
			new AudienceReply(channel.channel, false)
					.exec();
		}
	}

	public boolean isHandRaised(){
		return raisedHand;
	}

	public void setMuted(boolean muted){
		this.muted=muted;
		engine.muteLocalAudioStream(muted);
	}

	public boolean isMuted(){
		return muted;
	}

	public Channel getChannel(){
		return channel;
	}

	public void updateChannel(Channel chan){
		channel=chan;
		isSelfModerator=false;
		isSelfSpeaker=false;
		int id=Integer.parseInt(ClubhouseSession.userID);
		for(ChannelUser user:channel.users){
			if(user.userId==id){
				isSelfModerator=user.isModerator;
				isSelfSpeaker=user.isSpeaker;
				break;
			}
		}
	}

	private void callAddedListener(ChannelEventListener l){
		l.onChannelUpdated(channel);
	}

	public static void addListener(ChannelEventListener l){
		if(!listeners.contains(l))
			listeners.add(l);
		if(getInstance()!=null){
			getInstance().callAddedListener(l);
		}
	}

	public static void removeListener(ChannelEventListener l){
		listeners.remove(l);
	}

	public boolean isSelfSpeaker(){
		return isSelfSpeaker;
	}

	public boolean isSelfModerator(){
		return isSelfModerator;
	}

	private void onInvitedAsSpeaker(JsonObject msg){
		String ch=msg.get("channel").getAsString();
		if(!ch.equals(channel.channel))
			return;
		uiHandler.post(new Runnable(){
			@Override
			public void run(){
				for(ChannelEventListener l:listeners)
					l.onCanSpeak(msg.get("from_name").getAsString(), msg.get("from_user_id").getAsInt());
			}
		});
	}

	private void onUserJoined(JsonObject msg){
		String ch=msg.get("channel").getAsString();
		if(!ch.equals(channel.channel))
			return;
		JsonObject profile=msg.getAsJsonObject("user_profile");
		ChannelUser user=ClubhouseAPIController.getInstance().getGson().fromJson(profile, ChannelUser.class);
		uiHandler.post(new Runnable(){
			@Override
			public void run(){
				channel.users.add(user);
				for(ChannelEventListener l:listeners)
					l.onUserJoined(user);
			}
		});
	}

	private void onUserLeft(JsonObject msg){
		String ch=msg.get("channel").getAsString();
		if(!ch.equals(channel.channel))
			return;
		int id=msg.get("user_id").getAsInt();
		uiHandler.post(new Runnable(){
			@Override
			public void run(){
				for(ChannelUser user:channel.users){
					if(user.userId==id){
						channel.users.remove(user);
						break;
					}
				}
				for(ChannelEventListener l:listeners)
					l.onUserLeft(id);
			}
		});
	}

	private void onEndChannel(JsonObject msg){
		String ch=msg.get("channel").getAsString();
		if(!ch.equals(channel.channel))
			return;
		uiHandler.post(new Runnable(){
			@Override
			public void run(){
				for(ChannelEventListener l:listeners)
					l.onChannelEnded();
			}
		});
		leaveChannel();
	}

	public interface ChannelEventListener{
		void onUserMuteChanged(int id, boolean muted);
		void onUserJoined(ChannelUser user);
		void onUserLeft(int id);
		void onCanSpeak(String inviterName, int inviterID);
		void onChannelUpdated(Channel channel);
		void onSpeakingUsersChanged(List<Integer> ids);
		void onChannelEnded();
		void onSelfLeft();
	}

	private class RtcEngineEventHandler extends IRtcEngineEventHandler{
		@Override
		public void onJoinChannelSuccess(String channel, int uid, int elapsed){
			Log.d(TAG, "onJoinChannelSuccess() called with: channel = ["+channel+"], uid = ["+uid+"], elapsed = ["+elapsed+"]");
		}

		@Override
		public void onError(int err){
			Log.d(TAG, "onError() called with: err = ["+err+"]");
		}

		@Override
		public void onAudioVolumeIndication(AudioVolumeInfo[] speakers, int totalVolume){
//			Log.d(TAG, "onAudioVolumeIndication() called with: speakers = ["+Arrays.toString(speakers)+"], totalVolume = ["+totalVolume+"]");
			uiHandler.post(new Runnable(){
				@Override
				public void run(){
					int selfID=Integer.parseInt(ClubhouseSession.userID);
					List<Integer> uids=Arrays.stream(speakers).map(s -> s.uid==0 ? selfID : s.uid).collect(Collectors.toList());
					for(ChannelEventListener l:listeners)
						l.onSpeakingUsersChanged(uids);
				}
			});
		}

		@Override
		public void onUserMuteAudio(int uid, boolean muted){
//			Log.d(TAG, "onUserMuteAudio() called with: uid = ["+uid+"], muted = ["+muted+"]");
			uiHandler.post(new Runnable(){
				@Override
				public void run(){
							for(ChannelUser u:channel.users){
								if(u.userId==uid){
									u.isMuted=muted;
									break;
								}
							}
							for(ChannelEventListener l:listeners)
								l.onUserMuteChanged(uid, muted);
				}
			});
		}
	}
}
