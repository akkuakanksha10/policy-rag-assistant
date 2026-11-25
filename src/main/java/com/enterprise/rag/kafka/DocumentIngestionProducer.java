package com.enterprise.rag.kafka;

import com.enterprise.rag.config.RagProperties;
import com.enterprise.rag.dto.AsyncIngestResponse;
import com.enterprise.rag.dto.IngestRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
public class DocumentIngestionProducer {

    private static final Logger log = LoggerFactory.getLogger(DocumentIngestionProducer.class);

    private final KafkaTemplate<String, DocumentUploadedEvent> kafkaTemplate;
    private final RagProperties props;

    public DocumentIngestionProducer(KafkaTemplate<String, DocumentUploadedEvent> kafkaTemplate,
                                     RagProperties props) {
        this.kafkaTemplate = kafkaTemplate;
        this.props = props;
    }

    public AsyncIngestResponse publish(IngestRequest request) {
        String eventId = UUID.randomUUID().toString();
        String topic = props.kafka().ingestTopic();

        DocumentUploadedEvent event = new DocumentUploadedEvent(
                eventId,
                request.sourceName().trim(),
                request.department().trim().toUpperCase(),
                request.accessLevel().trim().toUpperCase(),
                request.content(),
                Instant.now()
        );

        // Key by sourceName so updates to the same policy document stay ordered within a partition
        kafkaTemplate.send(topic, event.sourceName(), event);
        log.info("Published DocumentUploadedEvent eventId={} source={} topic={}",
                eventId, event.sourceName(), topic);

        return new AsyncIngestResponse(eventId, event.sourceName(), "QUEUED", topic);
    }
}
