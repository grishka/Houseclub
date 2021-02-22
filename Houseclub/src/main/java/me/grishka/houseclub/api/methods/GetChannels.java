package me.grishka.houseclub.api.methods;

import java.util.List;

import me.grishka.houseclub.api.ClubhouseAPIRequest;
import me.grishka.houseclub.api.model.Channel;

public class GetChannels extends ClubhouseAPIRequest<GetChannels.Response> {
    public GetChannels() {
        super("GET", "get_channels", Response.class);
    }

    public static class Response {
        public List<Channel> channels;
    }
}
