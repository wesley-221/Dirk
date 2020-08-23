package com.beneluwux;

import com.beneluwux.helper.Log;
import com.beneluwux.helper.RegisterListener;
import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;
import org.javacord.api.listener.GloballyAttachableListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class DiscordConfiguration {
    private final ApplicationContext applicationContext;
    private final String discordApiKey;
    private DiscordApi discordApi;

    @Autowired
    public DiscordConfiguration(ApplicationContext applicationContext, @Value("${discord.api.key}") String discordApiKey) {
        this.applicationContext = applicationContext;
        this.discordApiKey = discordApiKey;
    }

    @Bean
    public void startDiscordBot() {
        this.discordApi = new DiscordApiBuilder()
                .setToken(discordApiKey)
                .login()
                .join();

        initialize();
    }

    @Bean
    public DiscordApi getDiscordApi() {
        return this.discordApi;
    }

    public void initialize() {
        Map<String, RegisterListener> listeners = applicationContext.getBeansOfType(RegisterListener.class);

        // Register all listeners with RegisterListener marker interface
        for (RegisterListener listener : listeners.values()) {
            discordApi.addListener((GloballyAttachableListener) listener);
            Log.info("Registered listener " + listener.getClass().getName());
        }

        discordApi.updateActivity("patat > friet");

        Log.info("Successfully initialized");
        Log.info("Discord invite link: " + discordApi.createBotInvite());
    }
}
