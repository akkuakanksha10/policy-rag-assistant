package com.enterprise.rag.config;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.ollama.OllamaChatModel;
import dev.langchain4j.model.ollama.OllamaEmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.pgvector.PgVectorEmbeddingStore;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
@EnableConfigurationProperties(RagProperties.class)
public class RagConfig {

    @Bean
    public ChatModel chatModel(RagProperties props) {
        return OllamaChatModel.builder()
                .baseUrl(props.ollama().baseUrl())
                .modelName(props.ollama().chatModel())
                .temperature(props.ollama().temperature())
                .timeout(Duration.ofSeconds(props.ollama().timeoutSeconds()))
                .build();
    }

    @Bean
    public EmbeddingModel embeddingModel(RagProperties props) {
        return OllamaEmbeddingModel.builder()
                .baseUrl(props.ollama().baseUrl())
                .modelName(props.ollama().embeddingModel())
                .timeout(Duration.ofSeconds(props.ollama().timeoutSeconds()))
                .build();
    }

    @Bean
    public EmbeddingStore<TextSegment> embeddingStore(RagProperties props) {
        return PgVectorEmbeddingStore.builder()
                .host(props.pgvector().host())
                .port(props.pgvector().port())
                .database(props.pgvector().database())
                .user(props.pgvector().user())
                .password(props.pgvector().password())
                .table(props.pgvector().table())
                .dimension(props.pgvector().dimension())
                .createTable(true)
                .build();
    }
}
