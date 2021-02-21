package me.grishka.houseclub.api.methods;

import me.grishka.houseclub.api.BaseResponse;
import me.grishka.houseclub.api.ClubhouseAPIRequest;

public class Follow extends ClubhouseAPIRequest<BaseResponse> {
    public Follow(int userID) {
        super("POST", "follow", BaseResponse.class);
        requestBody = new Body(userID);
    }

    private static class Body {
        public int userId, source = 4;

        public Body(int userId) {
            this.userId = userId;
        }
    }
}
