package com.beneluwux;

import com.beneluwux.helper.Settings;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
public class DatabaseConfiguration {
    Settings settings = new Settings();

    @Bean
    public DataSource getDataSource() {
        DataSourceBuilder dataSourceBuilder = DataSourceBuilder.create();

        dataSourceBuilder.url(settings.databaseUrl);
        dataSourceBuilder.username(settings.databaseUsername);
        dataSourceBuilder.password(settings.databasePassword);

        return dataSourceBuilder.build();
    }
}
