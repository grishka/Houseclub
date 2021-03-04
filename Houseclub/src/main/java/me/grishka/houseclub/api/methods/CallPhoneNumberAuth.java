package me.grishka.houseclub.api.methods;

import me.grishka.houseclub.api.BaseResponse;
import me.grishka.houseclub.api.ClubhouseAPIRequest;

public class CallPhoneNumberAuth extends ClubhouseAPIRequest<BaseResponse> {
	public CallPhoneNumberAuth(String phoneNumber){
		super("POST", "call_phone_number_auth", BaseResponse.class);
		requestBody=new Body(phoneNumber);
	}

	private static class Body{
		public String phoneNumber;

		public Body(String phoneNumber){
			this.phoneNumber=phoneNumber;
		}
	}
}
