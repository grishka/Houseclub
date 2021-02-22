package me.grishka.houseclub.api.methods;

import me.grishka.houseclub.api.BaseResponse;
import me.grishka.houseclub.api.ClubhouseAPIRequest;

public class Unfollow extends ClubhouseAPIRequest<BaseResponse> {
    public Unfollow(int userID) {
        super("POST", "unfollow", BaseResponse.class);
        requestBody = new Body(userID);
    }

    private static class Body {
        public int userId;

        public Body(int userId) {
            this.userId = userId;
        }
    }
}
