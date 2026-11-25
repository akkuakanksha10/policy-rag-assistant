package com.enterprise.rag.service;

import com.enterprise.rag.dto.IngestRequest;
import com.enterprise.rag.dto.IngestResponse;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class DocumentIngestionService {

    private final EmbeddingModel embeddingModel;
    private final EmbeddingStore<TextSegment> embeddingStore;

    public DocumentIngestionService(EmbeddingModel embeddingModel,
                                    EmbeddingStore<TextSegment> embeddingStore) {
        this.embeddingModel = embeddingModel;
        this.embeddingStore = embeddingStore;
    }

    public IngestResponse ingest(IngestRequest request) {
        long start = System.currentTimeMillis();

        String normalizedAccess = request.accessLevel().trim().toUpperCase();
        Metadata metadata = Metadata.from(Map.of(
                "source", request.sourceName().trim(),
                "department", request.department().trim().toUpperCase(),
                "access_level", normalizedAccess
        ));

        Document document = Document.from(request.content(), metadata);
        List<TextSegment> segments = DocumentSplitters.recursive(500, 60).split(document);

        List<Embedding> embeddings = embeddingModel.embedAll(segments).content();
        embeddingStore.addAll(embeddings, segments);

        long elapsed = System.currentTimeMillis() - start;
        return new IngestResponse(request.sourceName(), normalizedAccess, segments.size(), elapsed);
    }
}
