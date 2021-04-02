package me.grishka.houseclub.api.methods;

import me.grishka.houseclub.api.BaseResponse;
import me.grishka.houseclub.api.ClubhouseAPIRequest;

public class StartPhoneNumberAuth extends ClubhouseAPIRequest<BaseResponse> {
    public StartPhoneNumberAuth(String phoneNumber) {
        super("POST", "start_phone_number_auth", BaseResponse.class);
        requestBody = new Body(phoneNumber);
    }

    private static class Body {
        public String phoneNumber;

        public Body(String phoneNumber) {
            this.phoneNumber = phoneNumber;
        }
    }
}
