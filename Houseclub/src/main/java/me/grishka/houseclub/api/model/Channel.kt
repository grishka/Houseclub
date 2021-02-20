package me.grishka.houseclub.api.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
class Channel(
    var creatorUserProfileId:Int = 0,
    var channelId:Int = 0,
    var channel: String? = null,
    var topic: String? = null,
    var isPrivate:Boolean = false,
    var isSocialMode:Boolean = false,
    var url: String? = null,
    var numOther:Int = 0,
    var hasBlockedSpeakers:Boolean = false,
    var isExploreChannel:Boolean = false,
    var numSpeakers:Int = 0,
    var numAll:Int = 0,
    var users: MutableList<ChannelUser?>? = null,
    var token: String? = null,
    var isHandraiseEnabled:Boolean = false,
    var pubnubToken: String? = null,
    var pubnubHeartbeatValue:Int = 0,
    var pubnubHeartbeatInterval:Int = 0,
) : Parcelable
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
