package com.beneluwux.listeners;

import com.beneluwux.helper.Emoji;
import com.beneluwux.helper.RegisterListener;
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
