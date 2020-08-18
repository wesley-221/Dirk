package com.beneluwux;

import com.beneluwux.helper.Log;
import com.beneluwux.helper.Settings;
import com.beneluwux.listeners.BeneluxListener;
import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;

public class Main {
    private static final Settings settings = new Settings();

    public static void main(String[] args) {
        DiscordApi discordApi = new DiscordApiBuilder()
                .setToken(settings.discordIsInProduction ? settings.discordProdApiKey : settings.discordDevApiKey)
                .login()
                .join();

        initialize(discordApi);
    }

    public static void initialize(DiscordApi discordApi) {
        discordApi.addListener(new BeneluxListener());
        discordApi.updateActivity("patat > friet");

        Log.info("Successfully initialized " + settings.name);
        Log.info("Discord invite link: " + discordApi.createBotInvite());
    }
}
