package com.beneluwux.repositories;

import com.beneluwux.models.entities.CustomCommand;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CustomCommandRepository extends CrudRepository<CustomCommand, Integer> {
    Boolean existsByNameAndServerSnowflake(String name, Long serverSnowflake);
    Boolean existsByNameAndServerSnowflakeOrServerSnowflake(String name, Long serverSnowflake1, Long serverSnowflake2);
    CustomCommand findByNameAndServerSnowflake(String name, Long serverSnowflake);
}
