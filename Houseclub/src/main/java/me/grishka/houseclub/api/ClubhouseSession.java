package me.grishka.houseclub.api;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.UUID;

import me.grishka.houseclub.App;
import me.grishka.houseclub.api.model.User;

public class ClubhouseSession {

    public static String deviceID, userID, userToken;
    public static boolean isWaitlisted;
    public static User self;

    public static void load() {
        SharedPreferences prefs = prefs();
        deviceID = prefs.getString("device_id", null);
        userID = prefs.getString("user_id", null);
        userToken = prefs.getString("user_token", null);
        isWaitlisted = prefs.getBoolean("waitlisted", false);
        if (deviceID == null) {
            deviceID = UUID.randomUUID().toString().toUpperCase();
            write();
        }
    }

    public static void write() {
        prefs().edit()
                .putString("device_id", deviceID)
                .putString("user_id", userID)
                .putString("user_token", userToken)
                .putBoolean("waitlisted", isWaitlisted)
                .apply();
    }

    public static boolean isLoggedIn() {
        return userID != null;
    }

    private static SharedPreferences prefs() {
        return App.applicationContext.getSharedPreferences("session", Context.MODE_PRIVATE);
    }
}
