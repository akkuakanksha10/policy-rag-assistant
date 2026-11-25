package com.enterprise.rag.kafka;

import com.enterprise.rag.config.RagProperties;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.TopicPartition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

@Configuration
public class KafkaTopicConfig {

    @Bean
    public NewTopic documentUploadedTopic(RagProperties props) {
        return TopicBuilder.name(props.kafka().ingestTopic())
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic documentUploadedDlqTopic(RagProperties props) {
        return TopicBuilder.name(props.kafka().dlqTopic())
                .partitions(1)
                .replicas(1)
                .build();
    }

    @Bean
    public DefaultErrorHandler kafkaErrorHandler(KafkaTemplate<Object, Object> kafkaTemplate,
                                                 RagProperties props) {
        DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(
                kafkaTemplate,
                (record, ex) -> new TopicPartition(props.kafka().dlqTopic(), 0)
        );
        FixedBackOff backOff = new FixedBackOff(
                props.kafka().retryBackoffMs(),
                props.kafka().retryAttempts()
        );
        return new DefaultErrorHandler(recoverer, backOff);
    }
}
