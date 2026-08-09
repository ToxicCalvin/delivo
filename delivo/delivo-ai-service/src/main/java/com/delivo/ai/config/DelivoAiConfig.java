package com.delivo.ai.config;

import com.delivo.ai.agent.IntentExtractorAiService;
import com.delivo.ai.agent.NlPolisherAiService;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.dashscope.QwenEmbeddingModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import dev.langchain4j.service.AiServices;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DelivoAiConfig {

    @Value("${langchain4j.open-ai.chat-model.api-key}")
    private String openAiApiKey;

    @Value("${langchain4j.open-ai.chat-model.model-name:gpt-5.4-nano}")
    private String openAiModelName;

    @Value("${langchain4j.dashscope.embedding-model.api-key}")
    private String dashScopeApiKey;

    @Value("${langchain4j.dashscope.embedding-model.model-name:text-embedding-v3}")
    private String embeddingModelName;


    @Bean
    public ChatLanguageModel chatLanguageModel() {
        return OpenAiChatModel.builder()
                .apiKey(openAiApiKey)
                .modelName(openAiModelName)
                .temperature(0.0)
                .build();
    }

    @Bean
    public StreamingChatLanguageModel streamingChatLanguageModel() {
        return OpenAiStreamingChatModel.builder()
                .apiKey(openAiApiKey)
                .modelName(openAiModelName)
                .temperature(0.0)
                .build();
    }

    @Bean
    public EmbeddingModel embeddingModel() {
        return QwenEmbeddingModel.builder()
                .apiKey(dashScopeApiKey)
                .modelName(embeddingModelName)
                .build();
    }

    @Bean
    public IntentExtractorAiService intentExtractorAiService(ChatLanguageModel chatLanguageModel) {
        return AiServices.builder(IntentExtractorAiService.class)
                .chatLanguageModel(chatLanguageModel)
                .build();
    }

    @Bean
    public NlPolisherAiService nlPolisherAiService(ChatLanguageModel chatLanguageModel) {
        return AiServices.builder(NlPolisherAiService.class)
                .chatLanguageModel(chatLanguageModel)
                .build();
    }
}
