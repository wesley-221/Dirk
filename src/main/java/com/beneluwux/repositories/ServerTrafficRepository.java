package com.beneluwux.repositories;

import com.beneluwux.models.entities.ServerTraffic;
import org.springframework.data.repository.CrudRepository;

public interface ServerTrafficRepository extends CrudRepository<ServerTraffic, Integer> {
    ServerTraffic findByServerSnowflakeAndChannelSnowflake(Long serverSnowflake, Long channelSnowflake);

    ServerTraffic findByServerSnowflake(Long serverSnowflake);
}
