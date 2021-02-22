package me.grishka.houseclub.api.methods;

import me.grishka.houseclub.api.BaseResponse;
import me.grishka.houseclub.api.ClubhouseAPIRequest;

public class UpdateBio extends ClubhouseAPIRequest<BaseResponse> {
    public UpdateBio(String bio) {
        super("POST", "update_bio", BaseResponse.class);
        requestBody = new Body(bio);
    }

    private static class Body {
        public String bio;

        public Body(String bio) {
            this.bio = bio;
        }
    }
}
