package me.grishka.houseclub.api.model

import android.os.Parcel
import android.os.Parcelable

class ChannelUser : User {
    var isSpeaker = false
    var isModerator = false
    var isFollowedBySpeaker = false
    var isInvitedAsSpeaker = false
    var isNew = false
    var timeJoinedAsSpeaker: String? = null
    var firstName: String? = null

    @Transient
    var isMuted = false
    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        super.writeToParcel(dest, flags)
        dest.writeByte(if (isSpeaker) 1.toByte() else 0.toByte())
        dest.writeByte(if (isModerator) 1.toByte() else 0.toByte())
        dest.writeByte(if (isFollowedBySpeaker) 1.toByte() else 0.toByte())
        dest.writeByte(if (isInvitedAsSpeaker) 1.toByte() else 0.toByte())
        dest.writeByte(if (isNew) 1.toByte() else 0.toByte())
        dest.writeString(timeJoinedAsSpeaker)
        dest.writeString(firstName)
    }

    override fun readFromParcel(source: Parcel) {
        super.readFromParcel(source)
        isSpeaker = source.readByte().toInt() != 0
        isModerator = source.readByte().toInt() != 0
        isFollowedBySpeaker = source.readByte().toInt() != 0
        isInvitedAsSpeaker = source.readByte().toInt() != 0
        isNew = source.readByte().toInt() != 0
        timeJoinedAsSpeaker = source.readString()
        firstName = source.readString()
    }

    constructor() {}
    protected constructor(`in`: Parcel) : super(`in`) {
        isSpeaker = `in`.readByte().toInt() != 0
        isModerator = `in`.readByte().toInt() != 0
        isFollowedBySpeaker = `in`.readByte().toInt() != 0
        isInvitedAsSpeaker = `in`.readByte().toInt() != 0
        isNew = `in`.readByte().toInt() != 0
        timeJoinedAsSpeaker = `in`.readString()
        firstName = `in`.readString()
    }

    companion object {
        val CREATOR: Parcelable.Creator<ChannelUser> = object : Parcelable.Creator<ChannelUser> {
            override fun createFromParcel(source: Parcel): ChannelUser {
                return ChannelUser(source)
            }

            override fun newArray(size: Int): Array<ChannelUser?> {
                return arrayOfNulls(size)
            }
        }
    }
}