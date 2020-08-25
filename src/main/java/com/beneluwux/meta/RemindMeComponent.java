package com.beneluwux.meta;

import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

@Component
public class RemindMeComponent {
    private final ScheduledExecutorService scheduler;

    public RemindMeComponent() {
        scheduler = Executors.newScheduledThreadPool(1);
    }

    @Bean
    public ScheduledExecutorService getScheduler() {
        return this.scheduler;
    }
}
