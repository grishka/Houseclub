package me.grishka.houseclub.api.methods;

import me.grishka.houseclub.api.BaseResponse;
import me.grishka.houseclub.api.ClubhouseAPIRequest;

public class LeaveChannel extends ClubhouseAPIRequest<BaseResponse> {
    public LeaveChannel(String channelName) {
        super("POST", "leave_channel", BaseResponse.class);
        requestBody = new Body(channelName);
    }

    private static class Body {
        public String channel, channelId;

        public Body(String channel) {
            this.channel = channel;
        }
    }
}
