package com.beneluwux.helper;

import org.javacord.api.entity.message.embed.EmbedBuilder;

import java.awt.*;

public class EmbedHelper {
    public static EmbedBuilder genericSuccessEmbed(String message, String author) {
        return new EmbedBuilder().setTimestampToNow().setColor(Color.GREEN).setAuthor("✅ Nicely done, " + author + "!").setDescription(message);
    }

    public static EmbedBuilder genericErrorEmbed(String message, String author) {
        return new EmbedBuilder().setTimestampToNow().setColor(Color.RED).setAuthor("❌ Something went wrong for " + author).setDescription(message);
    }
}
