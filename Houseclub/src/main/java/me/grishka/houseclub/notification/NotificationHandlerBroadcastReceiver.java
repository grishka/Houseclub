package me.grishka.houseclub.notification;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import java.util.Objects;

import me.grishka.houseclub.VoiceService;

public class NotificationHandlerBroadcastReceiver extends BroadcastReceiver {
    public static final String ACTION_LEAVE_ROOM = "ACTION_LEAVE_ROOM";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Objects.equals(intent.getAction(), ACTION_LEAVE_ROOM)) {
            if (VoiceService.getInstance() != null) {
                VoiceService.getInstance().leaveCurrentChannel();
            }
        }
    }
}
