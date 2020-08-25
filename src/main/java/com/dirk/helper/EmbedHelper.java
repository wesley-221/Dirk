/*
 * MIT License
 *
 * Copyright (c) 2020 Wesley
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.dirk.helper;

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

    public static EmbedBuilder reminderEmbed(String message) {
        return new EmbedBuilder().setTimestampToNow().setColor(Color.RED).setAuthor("Reminder").setDescription(message);
    }
}
