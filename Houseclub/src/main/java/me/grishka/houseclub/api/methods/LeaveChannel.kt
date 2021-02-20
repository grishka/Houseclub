package me.grishka.houseclub.api.methods

import me.grishka.houseclub.api.BaseResponse
import me.grishka.houseclub.api.ClubhouseAPIRequest

class LeaveChannel(channelName: String) :
    ClubhouseAPIRequest<BaseResponse?>("POST", "leave_channel", BaseResponse::class.java) {
    private class Body(var channel: String) {
        var channelId: String? = null
    }

    init {
        requestBody = Body(channelName)
    }
}