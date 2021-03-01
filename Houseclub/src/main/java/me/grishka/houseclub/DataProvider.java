package me.grishka.houseclub;

import androidx.annotation.Nullable;

import java.util.Objects;

import me.grishka.houseclub.api.model.Channel;

public class DataProvider {
    private static Channel channelCache = null;

    @Nullable
    public static Channel getChannel(String id) {
        if (channelCache == null) return null;
        return Objects.equals(channelCache.channel, id) ? channelCache : null;
    }

    public static Channel getCachedChannel(){
        return channelCache;
    }

    public static void saveChannel(Channel channel) {
        channelCache = channel;
    }
}
