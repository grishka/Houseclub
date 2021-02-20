package me.grishka.houseclub.api.model

import kotlinx.parcelize.Parcelize

@Parcelize
class ChannelUser(
    var isSpeaker: Boolean = false,
    var isModerator: Boolean = false,
    var isFollowedBySpeaker: Boolean = false,
    var isInvitedAsSpeaker: Boolean = false,
    var isNew: Boolean = false,
    var timeJoinedAsSpeaker: String? = null,
    var firstName: String? = null,
    @Transient var isMuted: Boolean = false
) : User() {

}