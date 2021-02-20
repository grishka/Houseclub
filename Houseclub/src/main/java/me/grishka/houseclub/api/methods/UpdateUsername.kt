package me.grishka.houseclub.api.methods

import me.grishka.houseclub.api.BaseResponse
import me.grishka.houseclub.api.ClubhouseAPIRequest

class UpdateUsername(name: String) :
    ClubhouseAPIRequest<BaseResponse?>("POST", "update_username", BaseResponse::class.java) {
    private class Body(var username: String)

    init {
        requestBody = Body(name)
    }
}