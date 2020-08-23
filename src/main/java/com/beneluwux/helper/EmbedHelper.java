package com.beneluwux.helper;

import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.user.User;

import java.awt.*;

public class EmbedHelper {
    public static EmbedBuilder genericSuccessEmbed(String message, String author) {
        return new EmbedBuilder().setTimestampToNow().setColor(Color.GREEN).setAuthor("✅ Nicely done, " + author + "!").setDescription(message);
    }

    public static EmbedBuilder genericErrorEmbed(String message, String author) {
        return new EmbedBuilder().setTimestampToNow().setColor(Color.RED).setAuthor("❌ Something went wrong for " + author).setDescription(message);
    }

    public static EmbedBuilder personJoinedServer(String message, User joinUser) {
        String parsedMessage = message
                .replace("{{tag}}", joinUser.getDiscriminatedName())
                .replace("{{userid}}", joinUser.getIdAsString());

        return new EmbedBuilder().setTimestampToNow().setFooter("User joined").setColor(Color.GREEN).setAuthor(parsedMessage, "", joinUser.getAvatar().getUrl().toString());
    }

    public static EmbedBuilder personLeaveServer(String message, User joinUser) {
        String parsedMessage = message
                .replace("{{tag}}", joinUser.getDiscriminatedName())
                .replace("{{userid}}", joinUser.getIdAsString());

        return new EmbedBuilder().setTimestampToNow().setFooter("User left").setColor(Color.ORANGE).setAuthor(parsedMessage, "", joinUser.getAvatar().getUrl().toString());
    }
}
