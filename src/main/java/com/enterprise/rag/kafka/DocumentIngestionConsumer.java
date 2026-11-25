package com.enterprise.rag.kafka;

import com.enterprise.rag.dto.IngestRequest;
import com.enterprise.rag.dto.IngestResponse;
import com.enterprise.rag.service.DocumentIngestionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

@Component
public class DocumentIngestionConsumer {

    private static final Logger log = LoggerFactory.getLogger(DocumentIngestionConsumer.class);

    private final DocumentIngestionService ingestionService;

    public DocumentIngestionConsumer(DocumentIngestionService ingestionService) {
        this.ingestionService = ingestionService;
    }

    @KafkaListener(topics = "${rag.kafka.ingest-topic}", groupId = "${spring.kafka.consumer.group-id}")
    public void onDocumentUploaded(DocumentUploadedEvent event,
                                   @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
                                   @Header(KafkaHeaders.OFFSET) long offset) {
        log.info("Consuming eventId={} source={} partition={} offset={}",
                event.eventId(), event.sourceName(), partition, offset);

        if (event.content() == null || event.content().isBlank() || event.content().contains("SIMULATE_FAILURE")) {
            throw new IllegalArgumentException(
                    "Invalid or corrupted document payload for source=" + event.sourceName());
        }

        IngestResponse result = ingestionService.ingest(new IngestRequest(
                event.sourceName(),
                event.department(),
                event.accessLevel(),
                event.content()
        ));

        log.info("Indexed eventId={} source={} chunks={} elapsedMs={}",
                event.eventId(), result.sourceName(), result.chunksStored(), result.elapsedMs());
    }

    @KafkaListener(topics = "${rag.kafka.dlq-topic}", groupId = "rag-dlq-auditor")
    public void onDeadLetter(DocumentUploadedEvent event,
                             @Header(name = KafkaHeaders.DLT_EXCEPTION_MESSAGE, required = false) String errorMessage) {
        log.error("DLQ ALERT -> eventId={} source={} reason={}",
                event.eventId(), event.sourceName(), errorMessage);
    }
}
