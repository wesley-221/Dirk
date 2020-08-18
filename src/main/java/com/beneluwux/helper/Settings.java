package com.beneluwux.helper;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

public class Settings {
    private static final String NAME = "name";
    private static final String DISCORD_PRODUCTION_API_KEY_FORMAT = "discord.prod.api";
    private static final String DISCORD_DEVELOPMENT_API_KEY_FORMAT = "discord.dev.api";
    private static final String DISCORD_IS_IN_PRODUCTION = "discord.production";
    private static final String DISCORD_COMMAND_PREFIX = "discord.prefix";

    public String name;
    public String discordProdApiKey;
    public String discordDevApiKey;
    public Boolean discordIsInProduction;
    public String discordCommandPrefix;

    public Settings() {
        Properties settings = new Properties();

        try {
            InputStream settingsFile = new FileInputStream("src/main/resources/settings.properties");
            settings.load(settingsFile);

            this.name = settings.getProperty(NAME);
            this.discordProdApiKey = settings.getProperty(DISCORD_PRODUCTION_API_KEY_FORMAT);
            this.discordDevApiKey = settings.getProperty(DISCORD_DEVELOPMENT_API_KEY_FORMAT);
            this.discordIsInProduction = Boolean.valueOf(settings.getProperty(DISCORD_IS_IN_PRODUCTION));
            this.discordCommandPrefix = settings.getProperty(DISCORD_COMMAND_PREFIX);
        }
        catch(Exception ex) {
            Log.debug(ex.getMessage());
        }
    }
}
