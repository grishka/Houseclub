package me.grishka.houseclub.api.methods

import me.grishka.houseclub.api.BaseResponse
import me.grishka.houseclub.api.ClubhouseAPIRequest

class AcceptSpeakerInvite(channel: String, userID: Int) :
    ClubhouseAPIRequest<BaseResponse?>("POST", "accept_speaker_invite", BaseResponse::class.java) {
    private class Body(var channel: String, var userId: Int)

    init {
        requestBody = Body(channel, userID)
    }
}