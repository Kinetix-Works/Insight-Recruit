package com.insight_recruit.backend.app.config;

import com.insight_recruit.backend.app.worker.CandidateQueueSubscriber;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisSubscriberConfig {

    @Bean
    public ChannelTopic candidateUploadTopic(RedisQueueProperties queueProperties) {
        String topic = queueProperties.candidateUploadTopic();
        return new ChannelTopic(topic == null || topic.isBlank() ? "insightrecruit:candidates:uploaded" : topic);
    }

    @Bean
    public MessageListenerAdapter candidateListenerAdapter(CandidateQueueSubscriber subscriber) {
        MessageListenerAdapter adapter = new MessageListenerAdapter(subscriber, "onMessage");
        adapter.setSerializer(new StringRedisSerializer());
        return adapter;
    }

    @Bean
    public RedisMessageListenerContainer redisContainer(
        RedisConnectionFactory connectionFactory,
        MessageListenerAdapter candidateListenerAdapter,
        ChannelTopic candidateUploadTopic
    ) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.addMessageListener(candidateListenerAdapter, candidateUploadTopic);
        return container;
    }
}
