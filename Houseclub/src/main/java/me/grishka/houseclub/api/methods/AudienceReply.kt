package me.grishka.houseclub.api.methods

import me.grishka.houseclub.api.BaseResponse
import me.grishka.houseclub.api.ClubhouseAPIRequest

class AudienceReply(channel: String, raise: Boolean) :
    ClubhouseAPIRequest<BaseResponse?>("POST", "audience_reply", BaseResponse::class.java) {
    private class Body(var channel: String, var raiseHands: Boolean, var unraiseHands: Boolean)

    init {
        requestBody = Body(channel, raise, !raise)
    }
}