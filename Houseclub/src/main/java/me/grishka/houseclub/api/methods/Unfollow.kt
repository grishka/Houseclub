package me.grishka.houseclub.api.methods

import me.grishka.houseclub.api.BaseResponse
import me.grishka.houseclub.api.ClubhouseAPIRequest

class Unfollow(userID: Int) : ClubhouseAPIRequest<BaseResponse?>("POST", "unfollow", BaseResponse::class.java) {
    private class Body(var userId: Int)

    init {
        requestBody = Body(userID)
    }
}