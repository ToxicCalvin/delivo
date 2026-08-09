package com.delivo.ai.config;

import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.ChatMessageDeserializer;
import dev.langchain4j.data.message.ChatMessageSerializer;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;


@Slf4j
public class RedisChatMemoryStore implements ChatMemoryStore {

    private final StringRedisTemplate redisTemplate;
    private final String keyPrefix;
    private final Duration ttl;

    public RedisChatMemoryStore(StringRedisTemplate redisTemplate, String keyPrefix, Duration ttl) {
        this.redisTemplate = redisTemplate;
        this.keyPrefix = keyPrefix;
        this.ttl = ttl;
    }

    @Override
    public List<ChatMessage> getMessages(Object memoryId) {
        try {
            String key = buildKey(memoryId);
            String json = redisTemplate.opsForValue().get(key);
            if (json == null || json.isEmpty()) {
                return new ArrayList<>();
            }
            return ChatMessageDeserializer.messagesFromJson(json);
        } catch (Exception e) {
            log.error("Failed to read chat memory from Redis for memoryId={}", memoryId, e);
            return new ArrayList<>();
        }
    }

    @Override
    public void updateMessages(Object memoryId, List<ChatMessage> messages) {
        try {
            String key = buildKey(memoryId);
            if (messages == null || messages.isEmpty()) {
                redisTemplate.delete(key);
                return;
            }
            String json = ChatMessageSerializer.messagesToJson(messages);
            redisTemplate.opsForValue().set(key, json, ttl);
        } catch (Exception e) {
            log.error("Failed to write chat memory to Redis for memoryId={}", memoryId, e);
        }
    }

    @Override
    public void deleteMessages(Object memoryId) {
        try {
            String key = buildKey(memoryId);
            redisTemplate.delete(key);
        } catch (Exception e) {
            log.error("Failed to delete chat memory from Redis for memoryId={}", memoryId, e);
        }
    }

    private String buildKey(Object memoryId) {
        return keyPrefix + memoryId;
    }
}
