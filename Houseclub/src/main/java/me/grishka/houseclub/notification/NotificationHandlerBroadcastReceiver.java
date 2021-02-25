package me.grishka.houseclub.notification;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import java.util.Objects;

import me.grishka.houseclub.VoiceService;

public class NotificationHandlerBroadcastReceiver extends BroadcastReceiver {
    public static final String ACTION_LEAVE_ROOM = "ACTION_LEAVE_ROOM";
    public static final String ACTION_TOGGLE_MUTE_SPEAKER = "ACTION_TOGGLE_MUTE_SPEAKER";

    @Override
    public void onReceive(Context context, Intent intent) {
        VoiceService svc=VoiceService.getInstance();
        if(svc==null)return;
        if (Objects.equals(intent.getAction(), ACTION_LEAVE_ROOM)) {
            svc.leaveCurrentChannel();
        }else if (Objects.equals(intent.getAction(), ACTION_TOGGLE_MUTE_SPEAKER)) {
            svc.setSpeakerMuted(!svc.isSpeakerMuted());
        }
    }
}
