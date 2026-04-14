package com.fpl.kafka;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
@ConditionalOnProperty(name = "fpl.kafka.enabled", havingValue = "true")
public class FplKafkaTopicConfig {

    @Bean
    public NewTopic fplSyncCommandsTopic(@Value("${fpl.kafka.topics.sync-commands}") String name) {
        return TopicBuilder.name(name).partitions(3).replicas(1).build();
    }
}
