package me.grishka.houseclub.api.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.Date

@Parcelize
class FullUser(
    var dsplayname: String? = null,
    var bio: String? = null,
    var twitter: String? = null,
    var instagram: String? = null,
    var numFollowers: Int = 0,
    var numFollowing: Int = 0,
    var followsMe: Boolean = false,
    var isBlockedByNetwork: Boolean = false,
    var timeCreated: Date? = null,
    var invitedByUserProfile: User? = null,
    // null = not following
    // 2 = following
    // other values = ?
    var notificationType: Int = 0,

    ) : User(),Parcelable {
    val isFollowed: Boolean
        get() = notificationType == 2
}