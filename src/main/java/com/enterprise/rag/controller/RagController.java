package com.enterprise.rag.controller;

import com.enterprise.rag.dto.AskRequest;
import com.enterprise.rag.dto.AskResponse;
import com.enterprise.rag.dto.AsyncIngestResponse;
import com.enterprise.rag.dto.IngestRequest;
import com.enterprise.rag.dto.IngestResponse;
import com.enterprise.rag.kafka.DocumentIngestionProducer;
import com.enterprise.rag.service.DocumentIngestionService;
import com.enterprise.rag.service.PolicyQueryService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class RagController {

    private final DocumentIngestionService ingestionService;
    private final DocumentIngestionProducer ingestionProducer;
    private final PolicyQueryService queryService;

    public RagController(DocumentIngestionService ingestionService,
                         DocumentIngestionProducer ingestionProducer,
                         PolicyQueryService queryService) {
        this.ingestionService = ingestionService;
        this.ingestionProducer = ingestionProducer;
        this.queryService = queryService;
    }

    @PostMapping("/ingest")
    public IngestResponse ingestSync(@Valid @RequestBody IngestRequest request) {
        return ingestionService.ingest(request);
    }

    @PostMapping("/ingest/async")
    public ResponseEntity<AsyncIngestResponse> ingestAsync(@Valid @RequestBody IngestRequest request) {
        return ResponseEntity.accepted().body(ingestionProducer.publish(request));
    }

    @PostMapping("/ask")
    public AskResponse ask(@Valid @RequestBody AskRequest request) {
        return queryService.ask(request);
    }

    @PostMapping("/ingest/samples/async")
    public ResponseEntity<List<AsyncIngestResponse>> ingestSamplesAsync() {
        List<AsyncIngestResponse> queued = sampleRequests().stream()
                .map(ingestionProducer::publish)
                .toList();
        return ResponseEntity.accepted().body(queued);
    }

    @PostMapping("/ingest/samples")
    public List<IngestResponse> ingestSamplesSync() {
        return sampleRequests().stream()
                .map(ingestionService::ingest)
                .toList();
    }

    private List<IngestRequest> sampleRequests() {
        return List.of(
                new IngestRequest(
                        "remote-work-policy-2026.txt",
                        "HR",
                        "PUBLIC",
                        """
                        Remote and Hybrid Work Policy (Effective 2026):
                        Standard employees work hybrid: 3 days in office (Tuesday, Wednesday, Thursday) and 2 days remote.
                        Up to 15 business days per calendar year may be worked fully remote from another city within India with manager approval submitted 5 business days in advance.
                        International remote work requires Information Security and Tax Compliance sign-off at least 30 days prior.
                        """
                ),
                new IngestRequest(
                        "expense-reimbursement-2026.txt",
                        "FINANCE",
                        "PUBLIC",
                        """
                        Expense Reimbursement Policy:
                        Home office ergonomic equipment (monitor, keyboard, chair) is reimbursable up to INR 25,000 once every 24 months with GST invoices uploaded in the Expense Portal.
                        Team dinner claims require itemized receipts and are capped at INR 1,500 per attendee.
                        All claims must be submitted within 30 days of the transaction date.
                        """
                ),
                new IngestRequest(
                        "incident-escalation-confidential.txt",
                        "SECURITY",
                        "CONFIDENTIAL",
                        """
                        CONFIDENTIAL — Severity-1 Incident Escalation Runbook (Managers & Directors Only):
                        If a production data breach or unauthorized PII exposure is suspected, notify the CISO bridge line within 15 minutes and freeze affected IAM roles immediately.
                        External regulatory notification to CERT-In must be reviewed by Legal Counsel and filed within 6 hours of confirmation.
                        Do not discuss root cause details in public Slack or Teams channels until the post-incident report is approved.
                        """
                )
        );
    }
}
