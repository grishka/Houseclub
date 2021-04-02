package me.grishka.houseclub.api.methods;

import me.grishka.houseclub.api.BaseResponse;
import me.grishka.houseclub.api.ClubhouseAPIRequest;

public class UpdateUsername extends ClubhouseAPIRequest<BaseResponse> {
    public UpdateUsername(String name) {
        super("POST", "update_username", BaseResponse.class);
        requestBody = new Body(name);
    }

    private static class Body {
        public String username;

        public Body(String username) {
            this.username = username;
        }
    }
}
