package me.grishka.houseclub.api.methods;

import java.util.TimeZone;

import me.grishka.houseclub.api.ClubhouseAPIRequest;

public class Me extends ClubhouseAPIRequest<Me.Response> {
    public Me() {
        super("POST", "me", Response.class);
        requestBody = new Body();
    }

    private static class Body {
        public boolean return_blocked_ids;
        public String timezone_identifier;
        public boolean return_following_ids;

        public Body() {
            this.return_blocked_ids = true;
            this.return_following_ids = true;
            this.timezone_identifier = TimeZone.getDefault().getDisplayName();
        }
    }

    public static class Response {
        public int num_invites;
    }
}
