package com.beneluwux.meta;

import com.beneluwux.models.entities.CustomCommand;
import com.beneluwux.repositories.CustomCommandRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CustomCommandComponent {
    private final CustomCommandRepository customCommandRepository;
    private List<CustomCommand> allCustomCommands;

    @Autowired
    public CustomCommandComponent(CustomCommandRepository customCommandRepository) {
        this.customCommandRepository = customCommandRepository;

        allCustomCommands = (List<CustomCommand>) customCommandRepository.findAll();
    }

    @Bean
    public List<CustomCommand> getAllCustomCommands() {
        return this.allCustomCommands;
    }

    @Bean
    public void refreshCustomCommandsFromJPA() {
        allCustomCommands = (List<CustomCommand>) customCommandRepository.findAll();
    }
}
