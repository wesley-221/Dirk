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

package com.dirk.listeners;

import com.dirk.helper.Emoji;
import com.dirk.helper.RegisterListener;
import org.javacord.api.event.message.reaction.ReactionAddEvent;
import org.javacord.api.event.message.reaction.ReactionRemoveEvent;
import org.javacord.api.listener.message.reaction.ReactionAddListener;
import org.javacord.api.listener.message.reaction.ReactionRemoveListener;
import org.springframework.stereotype.Component;

@Component
public class BeneluxListener implements ReactionAddListener, ReactionRemoveListener, RegisterListener {
    private static final String WHITELIST_CHANNEL = "server-roles";

    @Override
    public void onReactionAdd(ReactionAddEvent reaction) {
        String channelName = reaction.getServerTextChannel().get().getName();

        if (channelName.equals(WHITELIST_CHANNEL)) {
            if (reaction.getEmoji().equalsEmoji(Emoji.NETHERLANDS_FLAG)) {
                reaction.getApi().getRolesByName("Netherlands").stream().findFirst().ifPresent(reaction.getUser()::addRole);
            }

            if (reaction.getEmoji().equalsEmoji(Emoji.BELGIUM_FLAG)) {
                reaction.getApi().getRolesByName("Belgium").stream().findFirst().ifPresent(reaction.getUser()::addRole);
            }

            if (reaction.getEmoji().equalsEmoji(Emoji.LUXEMBOURG_FLAG)) {
                reaction.getApi().getRolesByName("Luxembourg").stream().findFirst().ifPresent(reaction.getUser()::addRole);
            }

            if (reaction.getEmoji().equalsEmoji(Emoji.BEER)) {
                reaction.getApi().getRolesByName("Alcoholic").stream().findFirst().ifPresent(reaction.getUser()::addRole);
            }

            if (reaction.getEmoji().equalsEmoji(Emoji.CAKE)) {
                reaction.getApi().getRolesByName("Birthday").stream().findFirst().ifPresent(reaction.getUser()::addRole);
            }
        }
    }

    @Override
    public void onReactionRemove(ReactionRemoveEvent reaction) {
        String channelName = reaction.getServerTextChannel().get().getName();

        if (channelName.equals(WHITELIST_CHANNEL)) {
            if (reaction.getEmoji().equalsEmoji(Emoji.NETHERLANDS_FLAG)) {
                reaction.getApi().getRolesByName("Netherlands").stream().findFirst().ifPresent(reaction.getUser()::removeRole);
            }

            if (reaction.getEmoji().equalsEmoji(Emoji.BELGIUM_FLAG)) {
                reaction.getApi().getRolesByName("Belgium").stream().findFirst().ifPresent(reaction.getUser()::removeRole);
            }

            if (reaction.getEmoji().equalsEmoji(Emoji.LUXEMBOURG_FLAG)) {
                reaction.getApi().getRolesByName("Luxembourg").stream().findFirst().ifPresent(reaction.getUser()::removeRole);
            }

            if (reaction.getEmoji().equalsEmoji(Emoji.BEER)) {
                reaction.getApi().getRolesByName("Alcoholic").stream().findFirst().ifPresent(reaction.getUser()::removeRole);
            }

            if (reaction.getEmoji().equalsEmoji(Emoji.CAKE)) {
                reaction.getApi().getRolesByName("Birthday").stream().findFirst().ifPresent(reaction.getUser()::removeRole);
            }
        }
    }
}
