package com.beneluwux.listeners;

import com.beneluwux.helper.Emoji;
import com.beneluwux.helper.RegisterListener;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.event.message.reaction.ReactionAddEvent;
import org.javacord.api.event.message.reaction.ReactionRemoveEvent;
import org.javacord.api.listener.message.MessageCreateListener;
import org.javacord.api.listener.message.reaction.ReactionAddListener;
import org.javacord.api.listener.message.reaction.ReactionRemoveListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.awt.*;

@Component
public class BeneluxListener implements MessageCreateListener, ReactionAddListener, ReactionRemoveListener, RegisterListener {
    private static final String WHITELIST_CHANNEL = "server-roles";

    @Value("${discord.prefix}")
    private String discordCommandPrefix;

    @Override
    public void onMessageCreate(MessageCreateEvent message) {
        if (message.getMessageContent().startsWith(discordCommandPrefix)) {
            String command = message.getMessageContent().substring(discordCommandPrefix.length());

            if (message.getMessageAuthor().isBotUser())
                return;

            if (command.equals("setupbeneluxserver") && message.getMessageAuthor().isServerAdmin()) {
                String stringBuilder = "Click on any of the emoji's on this message in order to get the appropriate role. \n" +
                        ":flag_nl: : My nationality is **Dutch**! \n" +
                        ":flag_be: : My nationality is **Belgian**! \n" +
                        ":flag_lu: : My nationality is **Luxembourgian**! \n" +
                        ":beer: : I want to have access to the drinking channels, here we post announcements when we do drinking games over discord on this server. \n" +
                        ":birthday: : I want to enlist my birthday on this server, and receive notifications for other people's birthdays (Not working yet :poop:).";

                EmbedBuilder embedBuilder = new EmbedBuilder()
                        .setColor(Color.RED)
                        .setDescription(stringBuilder);

                message.getChannel().sendMessage(embedBuilder).whenComplete((sendMessage, throwable) -> {
                    sendMessage.addReaction(Emoji.NETHERLANDS_FLAG);
                    sendMessage.addReaction(Emoji.BELGIUM_FLAG);
                    sendMessage.addReaction(Emoji.LUXEMBOURG_FLAG);
                    sendMessage.addReaction(Emoji.BEER);
                });
            }
        }
    }

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
        }
    }
}
