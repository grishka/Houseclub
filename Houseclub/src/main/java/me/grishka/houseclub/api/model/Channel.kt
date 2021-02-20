package me.grishka.houseclub.api.model

import android.os.Parcel
import android.os.Parcelable

class Channel : Parcelable {
    /*
	* "channels":[
{
"creator_user_profile_id":5468389,
"channel_id":15215112,
"channel":"xlJkYk6m",
"topic":null,
"is_private":false,
"is_social_mode":false,
"url":"https://www.joinclubhouse.com/room/xlJkYk6m",
"feature_flags":[
],
"club":null,
"club_name":null,
"club_id":null,
"welcome_for_user_profile":null,
"num_other":0,
"has_blocked_speakers":false,
"is_explore_channel":false,
"num_speakers":8,
"num_all":184,
"users":[
{
"user_id":877820863,
"name":"Валентин Кашпур",
"photo_url":"https://clubhouseprod.s3.amazonaws.com:443/877820863_2cd32360-0f8a-46c1-826e-ae88f66ebc36_thumbnail_250x250",
"is_speaker":true,
"is_moderator":true,
"time_joined_as_speaker":"2021-02-19T00:52:31.484403+00:00",
"is_followed_by_speaker":true,
"is_invited_as_speaker":true
},
{
"user_id":1808486887,
"name":"Bogdan Kalashnikov",
"photo_url":"https://clubhouseprod.s3.amazonaws.com:443/1808486887_b08a9768-71a5-4968-a5bd-f4998bea0a95_thumbnail_250x250",
"is_speaker":true,
"is_moderator":true,
"time_joined_as_speaker":"2021-02-19T02:26:02.364976+00:00",
"is_followed_by_speaker":true,
"is_invited_as_speaker":true
},
{
"user_id":261058534,
"name":"Yulia Lis",
"photo_url":"https://clubhouseprod.s3.amazonaws.com:443/261058534_a3a1f882-487b-450a-be69-6c5a268c6b38_thumbnail_250x250",
"is_speaker":true,
"is_moderator":true,
"time_joined_as_speaker":"2021-02-19T02:30:29.539969+00:00",
"is_followed_by_speaker":true,
"is_invited_as_speaker":true
}
]
	* */
    var creatorUserProfileId = 0
    var channelId = 0
    var channel: String? = null
    var topic: String? = null
    var isPrivate = false
    var isSocialMode = false
    var url: String? = null
    var numOther = 0
    var hasBlockedSpeakers = false
    var isExploreChannel = false
    var numSpeakers = 0
    var numAll = 0
    var users: MutableList<ChannelUser?>? = null
    var token: String? = null
    var isHandraiseEnabled = false
    var pubnubToken: String? = null
    var pubnubHeartbeatValue = 0
    var pubnubHeartbeatInterval = 0
    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeInt(creatorUserProfileId)
        dest.writeInt(channelId)
        dest.writeString(channel)
        dest.writeString(topic)
        dest.writeByte(if (isPrivate) 1.toByte() else 0.toByte())
        dest.writeByte(if (isSocialMode) 1.toByte() else 0.toByte())
        dest.writeString(url)
        dest.writeInt(numOther)
        dest.writeByte(if (hasBlockedSpeakers) 1.toByte() else 0.toByte())
        dest.writeByte(if (isExploreChannel) 1.toByte() else 0.toByte())
        dest.writeInt(numSpeakers)
        dest.writeInt(numAll)
        dest.writeTypedList(users)
        dest.writeString(token)
        dest.writeByte(if (isHandraiseEnabled) 1.toByte() else 0.toByte())
        dest.writeString(pubnubToken)
        dest.writeInt(pubnubHeartbeatValue)
        dest.writeInt(pubnubHeartbeatInterval)
    }

    fun readFromParcel(source: Parcel) {
        creatorUserProfileId = source.readInt()
        channelId = source.readInt()
        channel = source.readString()
        topic = source.readString()
        isPrivate = source.readByte().toInt() != 0
        isSocialMode = source.readByte().toInt() != 0
        url = source.readString()
        numOther = source.readInt()
        hasBlockedSpeakers = source.readByte().toInt() != 0
        isExploreChannel = source.readByte().toInt() != 0
        numSpeakers = source.readInt()
        numAll = source.readInt()
        users = source.createTypedArrayList(ChannelUser.CREATOR)
        token = source.readString()
        isHandraiseEnabled = source.readByte().toInt() != 0
        pubnubToken = source.readString()
        pubnubHeartbeatValue = source.readInt()
        pubnubHeartbeatInterval = source.readInt()
    }

    constructor() {}
    protected constructor(`in`: Parcel) {
        creatorUserProfileId = `in`.readInt()
        channelId = `in`.readInt()
        channel = `in`.readString()
        topic = `in`.readString()
        isPrivate = `in`.readByte().toInt() != 0
        isSocialMode = `in`.readByte().toInt() != 0
        url = `in`.readString()
        numOther = `in`.readInt()
        hasBlockedSpeakers = `in`.readByte().toInt() != 0
        isExploreChannel = `in`.readByte().toInt() != 0
        numSpeakers = `in`.readInt()
        numAll = `in`.readInt()
        users = `in`.createTypedArrayList(ChannelUser.CREATOR)
        token = `in`.readString()
        isHandraiseEnabled = `in`.readByte().toInt() != 0
        pubnubToken = `in`.readString()
        pubnubHeartbeatValue = `in`.readInt()
        pubnubHeartbeatInterval = `in`.readInt()
    }

    companion object {
        val CREATOR: Parcelable.Creator<Channel> = object : Parcelable.Creator<Channel> {
            override fun createFromParcel(source: Parcel): Channel {
                return Channel(source)
            }

            override fun newArray(size: Int): Array<Channel?> {
                return arrayOfNulls(size)
            }
        }
    }
}