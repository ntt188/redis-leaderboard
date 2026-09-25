package com.example.leaderboard;

import io.lettuce.core.ReadFrom;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisStaticMasterReplicaConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;

import java.time.Duration;

@Configuration
public class RedisConfig {

    // Ghi -> master, Đọc -> ưu tiên replica
    @Bean
    public LettuceConnectionFactory redisConnectionFactory(
            @Value("${redis.master.host}") String masterHost,
            @Value("${redis.master.port}") int masterPort,
            @Value("${redis.replica.host}") String replicaHost,
            @Value("${redis.replica.port}") int replicaPort) {

        RedisStaticMasterReplicaConfiguration config =
                new RedisStaticMasterReplicaConfiguration(masterHost, masterPort);
        config.addNode(replicaHost, replicaPort);

        LettuceClientConfiguration clientConfig = LettuceClientConfiguration.builder()
                .readFrom(ReadFrom.REPLICA_PREFERRED)
                .commandTimeout(Duration.ofSeconds(2)) // Redis sập -> báo lỗi nhanh, không treo 60s
                .build();

        return new LettuceConnectionFactory(config, clientConfig);
    }
}
