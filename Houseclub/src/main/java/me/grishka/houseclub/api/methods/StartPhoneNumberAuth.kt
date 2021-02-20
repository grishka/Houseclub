package me.grishka.houseclub.api.methods

import me.grishka.houseclub.api.BaseResponse
import me.grishka.houseclub.api.ClubhouseAPIRequest

class StartPhoneNumberAuth(phoneNumber: String) :
    ClubhouseAPIRequest<BaseResponse?>("POST", "start_phone_number_auth", BaseResponse::class.java) {
    private class Body(var phoneNumber: String)

    init {
        requestBody = Body(phoneNumber)
    }
}