package me.grishka.houseclub.api.methods

import me.grishka.houseclub.api.ClubhouseAPIRequest
import me.grishka.houseclub.api.model.User

class CompletePhoneNumberAuth(phoneNumber: String, code: String) :
    ClubhouseAPIRequest<CompletePhoneNumberAuth.Response?>("POST", "complete_phone_number_auth", Response::class.java) {
    class Response {
        var authToken: String? = null
        var accessToken: String? = null
        var refreshToken: String? = null
        var isWaitlisted = false
        var userProfile: User? = null
    }

    private class Body(var phoneNumber: String, var verificationCode: String)

    init {
        requestBody = Body(phoneNumber, code)
    }
}