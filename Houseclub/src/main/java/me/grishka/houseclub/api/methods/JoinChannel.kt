package me.grishka.houseclub.api.methods

import me.grishka.houseclub.api.ClubhouseAPIRequest
import me.grishka.houseclub.api.model.Channel

class JoinChannel(channelName: String) : ClubhouseAPIRequest<Channel?>("POST", "join_channel", Channel::class.java) {
    private class Body(var channel: String, var attributionSource: String, var attributionDetails: String)

    init {
        requestBody = Body(channelName, "feed", "eyJpc19leHBsb3JlIjpmYWxzZSwicmFuayI6MX0=")
    }
}