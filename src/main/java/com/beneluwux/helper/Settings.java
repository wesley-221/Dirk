package com.beneluwux.helper;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

public class Settings {
    private static final String NAME = "name";

    // Discord related settings
    private static final String DISCORD_PRODUCTION_API_KEY_FORMAT = "discord.prod.api";
    private static final String DISCORD_DEVELOPMENT_API_KEY_FORMAT = "discord.dev.api";
    private static final String DISCORD_IS_IN_PRODUCTION = "discord.production";
    private static final String DISCORD_COMMAND_PREFIX = "discord.prefix";

    // Database related settings
    private static final String DATABASE_URL = "database.url";
    private static final String DATABASE_USERNAME = "database.username";
    private static final String DATABASE_PASSWORD = "database.password";

    public String name;

    public String discordProdApiKey;
    public String discordDevApiKey;
    public Boolean discordIsInProduction;
    public String discordCommandPrefix;

    public String databaseUrl;
    public String databaseUsername;
    public String databasePassword;

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

            this.databaseUrl = settings.getProperty(DATABASE_URL);
            this.databaseUsername = settings.getProperty(DATABASE_USERNAME);
            this.databasePassword = settings.getProperty(DATABASE_PASSWORD);
        }
        catch(Exception ex) {
            Log.debug(ex.getMessage());
        }
    }
}
