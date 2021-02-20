package me.grishka.houseclub.api.methods

import me.grishka.houseclub.api.BaseResponse
import me.grishka.houseclub.api.ClubhouseAPIRequest

class ActivePing(channel: String) :
    ClubhouseAPIRequest<BaseResponse?>("POST", "active_ping", BaseResponse::class.java) {
    private class Body(var channel: String)

    init {
        requestBody = Body(channel)
    }
}