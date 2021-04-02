package me.grishka.houseclub.api.methods;

import me.grishka.houseclub.api.ClubhouseAPIRequest;
import me.grishka.houseclub.api.model.Channel;

public class JoinChannel extends ClubhouseAPIRequest<Channel> {

    public JoinChannel(String channelName) {
        super("POST", "join_channel", Channel.class);
        requestBody = new Body(channelName, "feed", "eyJpc19leHBsb3JlIjpmYWxzZSwicmFuayI6MX0=");
    }

    private static class Body {
        public String channel, attributionSource, attributionDetails;

        public Body(String channel, String attributionSource, String attributionDetails) {
            this.channel = channel;
            this.attributionSource = attributionSource;
            this.attributionDetails = attributionDetails;
        }
    }
}
