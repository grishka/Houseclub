package me.grishka.houseclub.api.model

import java.util.Date

class FullUser : User() {
    var dsplayname: String? = null
    var bio: String? = null
    var twitter: String? = null
    var instagram: String? = null
    var numFollowers = 0
    var numFollowing = 0
    var followsMe = false
    var isBlockedByNetwork = false
    var timeCreated: Date? = null
    var invitedByUserProfile: User? = null

    // null = not following
    // 2 = following
    // other values = ?
    var notificationType = 0
    val isFollowed: Boolean
        get() = notificationType == 2
}